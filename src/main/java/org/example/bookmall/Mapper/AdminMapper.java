package org.example.bookmall.Mapper;

import org.apache.ibatis.annotations.Param;
import org.example.bookmall.entity.Book;
import org.example.bookmall.entity.Order;
import org.example.bookmall.entity.OrderItem;
import org.example.bookmall.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 管理员数据访问层（操作数据库）
 * 注解SQL直接映射实体类，MyBatis自动完成字段转换（驼峰→下划线）
 */
@Repository // 标记为数据访问组件，Spring自动扫描
public interface AdminMapper {

    // 1. 管理员登录（查询is_admin=1的用户）
    @Select("SELECT * FROM [User] WHERE login_name=#{loginName} AND password=#{password} AND is_admin=1")
    User selectAdminByLoginNameAndPwd(String loginName, String password);

    // 2. 新增图书（适配Book表自增bid，实体类bookId对应表中bid）
    @Insert("INSERT INTO Book (book_name, author, price, sale_num, stock_num, book_type) " +
            "VALUES (#{bookName}, #{author}, #{price}, #{saleNum}, #{stockNum}, #{bookType})")
    int insertBook(Book book);

    // ========== 新增：重复图书校验 + 库存累加 ==========
    // 2.1 查询是否存在相同书名+作者+价格的图书
    @Select("SELECT COUNT(*) FROM Book WHERE book_name=#{bookName} AND author=#{author} AND ROUND(price, 2) = ROUND(#{price}, 2)")
    int countDuplicateBook(String bookName, String author, BigDecimal price);

    // 2.2 累加已有图书的库存
    // 必须加@Param注解，明确参数名！
    @Update("UPDATE Book SET stock_num = " +
            "CASE " +
            "   WHEN stock_num = -1 THEN #{addStock} " +
            "   ELSE stock_num + #{addStock} " +
            "END " +
            "WHERE book_name=#{bookName} AND author=#{author} AND ROUND(price, 2) = ROUND(#{price}, 2)")
    int addStockToExistBook(
            @Param("bookName") String bookName,
            @Param("author") String author,
            @Param("price") BigDecimal price,
            @Param("addStock") Integer addStock
    );

    // 3. 库存预警（查询stock_num≤阈值的图书）→ 核心修改：给bid加别名bookId，其他字段保留驼峰映射
    // 库存预警：只查1≤stock_num≤5的正常图书，排除下架(-1)和无货(0)
    // 修改：添加cover_url字段
    @Select("SELECT bid AS bookId, book_name, author, price, sale_num, stock_num, book_type, cover_url FROM Book WHERE stock_num BETWEEN 1 AND #{threshold}")
    List<Book> selectLowStockBooks(Integer threshold);

    // 4. 查看所有普通用户（排除管理员）
    @Select("SELECT * FROM [User] WHERE is_admin != 1 OR is_admin IS NULL")
    List<User> selectAllCommonUsers();

    // 5. 查看所有订单
    @Select("SELECT * FROM [Order]")
    List<Order> selectAllOrders();

    // 6. 修改图书库存（按bookId=bid修改）→ 这里逻辑本身正确，不用改
    @Update("UPDATE Book SET stock_num=#{newStock} WHERE bid=#{bookId}")
    int updateBookStock(Integer bookId, Integer newStock);

    // 7. 查看所有订单详情 → 核心修改：给bid加别名bookId
    @Select("SELECT id, order_id, user_id, bid AS bookId, book_name, num FROM OrderItem")
    List<OrderItem> selectAllOrderItems();
    /**
     * 按订单ID查询订单详情（修复：显式指定字段+bid AS bookId）
     */
    @Select("SELECT id, order_id, user_id, bid AS bookId, book_name, num FROM OrderItem WHERE order_id = #{orderId}")
    List<OrderItem> listOrderItemsByOrderId(Integer orderId);

    // 8. 查看所有图书（全量查询）→ 核心修改：给bid加别名bookId
    // 修改：添加cover_url字段
    // 修改后（把 DESC 改为 ASC 或直接删除）：
    @Select("SELECT bid AS bookId, book_name, author, price, sale_num, stock_num, book_type, cover_url FROM Book ORDER BY bid ASC")
    List<Book> selectAllBooks();

    // 9. 图书条件查询（扩展：图书ID + 作者，修复XML特殊字符转义）→ 核心修改：给bid加别名bookId
    // 修改：添加cover_url字段
    @Select("<script>" +
            "SELECT bid AS bookId, book_name, author, price, sale_num, stock_num, book_type, cover_url FROM Book WHERE 1=1 " +
            // 图书ID精准查询（注意：这里还是用bid，因为数据库字段是bid）
            "<if test='bookId != null'>" +
            "AND bid = #{bookId} " +
            "</if>" +
            // 书名模糊查询
            "<if test='bookName != null and bookName != \"\"'>" +
            "AND book_name LIKE CONCAT('%', #{bookName}, '%') " +
            "</if>" +
            // 作者模糊查询
            "<if test='author != null and author != \"\"'>" +
            "AND author LIKE CONCAT('%', #{author}, '%') " +
            "</if>" +
            // 图书类型精准查询
            "<if test='bookType != null and bookType != \"\"'>" +
            "AND book_type = #{bookType} " +
            "</if>" +
            // 最低价格（>= 转义为 &gt;=）
            "<if test='minPrice != null'>" +
            "AND price &gt;= #{minPrice} " +
            "</if>" +
            // 最高价格（<= 转义为 &lt;=）
            "<if test='maxPrice != null'>" +
            "AND price &lt;= #{maxPrice} " +
            "</if>" +
            "ORDER BY bid" +
            "</script>")
    List<Book> selectBooksByCondition(Integer bookId, String bookName, String author, String bookType, BigDecimal minPrice, BigDecimal maxPrice);

    // ========== 管理员销售榜相关方法 ==========
    /**
     * 1. 统计销量数据（修复：增加订单状态过滤+优化时间匹配）
     * 修改：添加cover_url字段
     */
    @Select("SELECT " +
            "b.bid AS bookId, " +
            "b.book_name AS bookName, " +  // 修复：字段名加AS，确保驼峰映射
            "b.author, " +
            "b.cover_url, " +  // 添加封面URL
            "COALESCE(SUM(oi.num), 0) AS totalSale, " +  // 无数据时默认0
            "COALESCE(SUM(oi.num * b.price), 0) AS totalRevenue " +
            "FROM Book b " +
            "INNER JOIN OrderItem oi ON b.bid = oi.bid " +  // 改为INNER JOIN，只保留有销售的图书
            "INNER JOIN [Order] o ON oi.order_id = o.order_id " +
            "WHERE 1=1 " +
            "AND o.state = 2 " +  // 关键：只统计已完成订单（匹配你的Order实体状态定义）
            "AND o.sale_time >= #{startTime} " +  // 优化时间条件，覆盖当天所有时间
            "AND o.sale_time <= #{endTime} " +
            "GROUP BY b.bid, b.book_name, b.author, b.price, b.cover_url " +  // 确保GROUP BY与SELECT字段一致
            "HAVING COALESCE(SUM(oi.num), 0) > 0 " +  // 过滤销量为0的记录
            "ORDER BY totalSale DESC")
    List<Map<String, Object>> statAdminSaleData(
            @Param("startTime") String startTime,  // 必须加@Param，避免参数混淆
            @Param("endTime") String
                    endTime
    );

    /**
     * 2. 导出报表（增加时间筛选）
     */
    @Select("<script>" +
            "SELECT " +
            "b.bid AS bookId, " +
            "b.book_name AS bookName, " +
            "b.author, " +
            "b.price, " +
            "b.sale_num AS saleNum, " +
            "b.stock_num AS stockNum, " +
            "b.cover_url, " +
            "COALESCE(SUM(oi.num), 0) AS totalSale, " +
            "COALESCE(SUM(oi.num * b.price), 0) AS totalRevenue, " +
            "ROW_NUMBER() OVER (ORDER BY COALESCE(SUM(oi.num), 0) DESC) AS rank " +
            "FROM Book b " +
            "LEFT JOIN OrderItem oi ON b.bid = oi.bid " +
            "LEFT JOIN [Order] o ON oi.order_id = o.order_id " +
            "WHERE b.stock_num != -1 " +
            "AND oi.order_id IS NOT NULL " +
            "AND o.state = 2 " +
            "<if test='startTime != null and endTime != null'>" +
            "AND o.sale_time &gt;= #{startTime} " +
            "AND o.sale_time &lt;= #{endTime} " +
            "</if>" +
            "GROUP BY b.bid, b.book_name, b.author, b.price, b.sale_num, b.stock_num, b.cover_url " +
            "HAVING COALESCE(SUM(oi.num), 0) > 0 " +
            "ORDER BY totalSale DESC" +
            "</script>")
    List<Map<String, Object>> exportAdminSaleReport(
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    // 新增：同步销量的方法（如果需要同步totalSale到sale_num）
    @Update("UPDATE Book SET sale_num = #{totalSale} WHERE bid = #{bookId}")
    int syncBookSaleNum(@Param("bookId") Integer bookId, @Param("totalSale") Integer totalSale);

    /**
     * 3. 按订单状态筛选销量（修复：字段名映射）
     * 修改：添加cover_url字段
     */
    @Select("SELECT " +
            "b.bid AS bookId, " +
            "b.book_name AS bookName, " +
            "b.cover_url, " +  // 添加封面URL
            "COALESCE(SUM(oi.num), 0) AS totalSale " +
            "FROM Book b " +
            "INNER JOIN OrderItem oi ON b.bid = oi.bid " +
            "INNER JOIN [Order] o ON oi.order_id = o.order_id " +
            "WHERE o.state = #{state} " +
            "GROUP BY b.bid, b.book_name, b.cover_url " +
            "HAVING COALESCE(SUM(oi.num), 0) > 0 " +
            "ORDER BY totalSale DESC")
    List<Map<String, Object>> statSaleByOrderState(Integer state);

    // ========== 新增：查询图书当前库存（解决精度问题） ==========
    @Select("SELECT stock_num FROM Book WHERE book_name=#{bookName} AND author=#{author} AND ROUND(price, 2) = ROUND(#{price}, 2)")
    Integer getBookCurrentStock(String bookName, String author, BigDecimal price);

    // ========== 新增：下架图书相关方法（stock_num=-1标记下架） ==========
    /**
     * 下架图书：将stock_num设为-1
     */
    @Update("UPDATE Book SET stock_num = -1 WHERE bid=#{bookId}")
    int logicDeleteBook(Integer bookId);

    /**
     * 校验图书是否存在且未下架（stock_num != -1）
     */
    @Select("SELECT COUNT(*) FROM Book WHERE bid = #{bookId} AND stock_num != -1")
    int checkBookExist(Integer bookId);

    // 修改后（根据你的表结构，应该是 bid 而不是 book_id）：
    @Update("UPDATE Book SET cover_url = #{coverUrl} WHERE bid = #{bookId}")
    int updateBookCover(Integer bookId, String coverUrl);
    /**
     * 更新图书信息
     */
    @Update("UPDATE Book SET book_name=#{bookName}, author=#{author}, price=#{price}, book_type=#{bookType} WHERE bid=#{bookId}")
    int updateBookInfo(Integer bookId, String bookName, String author, BigDecimal price, String bookType);
}