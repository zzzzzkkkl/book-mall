package org.example.bookmall.Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

//图书相关的sql语句
@Repository
public class BookOrderMapper {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    //SQL：模糊搜索图书（书名/作者）
    // 修改：添加cover_url字段
    public List<Map<String, Object>> searchBook(String keyword) {
        String sql = "SELECT bid, book_name, author, price, stock_num, cover_url FROM book WHERE (book_name LIKE ? OR author LIKE ?) AND stock_num>0";
        //%是LIKE的通配符
        return jdbcTemplate.queryForList(sql, "%" + keyword + "%", "%" + keyword + "%");
    }

    //SQL：根据书id获得图书
    // 修改：添加cover_url字段
    public List<Map<String, Object>> getBookById(Integer bookId) {
        String sql = "SELECT bid, book_name, author, price, stock_num, sale_num, book_type, cover_url FROM [BookStoreDB].[dbo].[Book] WHERE bid = ?";
        return jdbcTemplate.queryForList(sql, bookId);
    }


    //SQL：插入主订单，返回自增订单id
    public Integer insertMainOrder(Integer userId, BigDecimal totalPrice) {
        // 注意：必须写全表名，防止SQL Server表名冲突；state固定为2（已支付）
        String sql = "INSERT INTO [BookStoreDB].[dbo].[Order](user_id, sale_price, state) VALUES (?,?,2); SELECT ISNULL(SCOPE_IDENTITY(),0) AS order_id";
        // 强制转Integer，兜底返回0，杜绝null
        return jdbcTemplate.queryForObject(sql, new Object[]{userId, totalPrice}, Integer.class);
    }

    //SQL：插入单条订单明细
    public int insertOrderItem(Integer orderId, Integer userId, Integer bookId, Integer num) {
        String bookName = jdbcTemplate.queryForObject("SELECT book_name FROM [Book] WHERE bid=?", new Object[]{bookId}, String.class);
        String sql = "INSERT INTO [OrderItem](order_id, user_id, bid, num, book_name) VALUES (?,?,?,?,?)";
        // 返回受影响行数，用于上层校验
        return jdbcTemplate.update(sql, orderId, userId, bookId, num, bookName);
    }

    //SQL:创建订单
    public int createOrder(Integer userId, Integer bookId, Integer num) {
        //先查询图书单价+图书名称（提前拿数据，避免重复查询）
        String bookSql = "SELECT price, book_name FROM [Book] WHERE bid=?";
        Map<String, Object> bookMap = jdbcTemplate.queryForMap(bookSql, bookId);
        BigDecimal price = new BigDecimal(bookMap.get("price").toString()); // 图书单价
        String bookName = bookMap.get("book_name").toString(); // 图书名称
        BigDecimal totalPrice = price.multiply(new BigDecimal(num)); // 计算订单总价

        //先插入主订单Order，获取自增的orderId
        String insertOrderSql = "INSERT INTO [BookStoreDB].[dbo].[Order](user_id, sale_price, state) VALUES (?,?,?); SELECT SCOPE_IDENTITY()";
        //state=2 已支付（无支付功能，创建即支付）；SCOPE_IDENTITY() 获取自增的orderId
        Integer orderId = jdbcTemplate.queryForObject(insertOrderSql, new Object[]{userId, totalPrice, 2}, Integer.class);

        //调用insertOrderItem，插入明细表（复用正确代码）
        this.insertOrderItem(orderId, userId, bookId, num);

        //返回自增orderId（上层可拿到订单号，便于前端展示）
        return orderId;
    }

    //SQL:查询个人订单列表
    public List<Map<String, Object>> getMyOrder(Integer userId) {
        String sql = "SELECT * FROM [Order] WHERE user_id=?";
        return jdbcTemplate.queryForList(sql, userId);
    }

    //SQL：查询个人订单详情
    public List<Map<String, Object>> getOrderDetail(Integer orderId) {
        String sql = "SELECT oi.order_id, oi.user_id, oi.bid, oi.book_name, oi.num, b.price AS bookPrice, b.author AS bookAuthor, b.cover_url " +  // 添加cover_url
                "FROM [OrderItem] oi " +
                "LEFT JOIN [Book] b ON oi.bid = b.bid " +
                "WHERE oi.order_id=?";
        return jdbcTemplate.queryForList(sql, orderId);
    }

    //SQL：查询销量TOP10的图书
    // 修改：添加cover_url字段
    public List<Map<String, Object>> getHotBookTop10() {
        String sql = "SELECT TOP 10 b.bid, b.book_name, b.price, b.author, b.stock_num, b.book_type, b.cover_url, " +
                "ISNULL(s.sale_num, 0) AS sale_num " +
                "FROM [Book] b " +
                "LEFT JOIN (SELECT bid, SUM(num) AS sale_num FROM [OrderItem] GROUP BY bid) s " +
                "ON b.bid = s.bid " +
                "ORDER BY ISNULL(s.sale_num, 0) DESC";
        return jdbcTemplate.queryForList(sql);
    }

    // 扣减图书库存（乐观锁：确保库存>=购买数量才扣减,防止超卖）
    public int reduceBookStock(Integer bookId, Integer num) {
        // 乐观锁：WHERE stock_num >= ? 防止库存不足时扣减
        String sql = "UPDATE [Book] SET stock_num = stock_num - ? WHERE bid = ? AND stock_num >= ?";
        // 返回受影响行数：1=扣减成功；0=库存不足/图书不存在
        return jdbcTemplate.update(sql, num, bookId, num);
    }

    // 获取所有图书（按销量排序，分页）
    //添加cover_url字段
    public List<Map<String, Object>> getAllBooksByPage(int offset, int pageSize) {
        String sql = "SELECT bid, book_name, author, price, stock_num, sale_num, book_type, cover_url FROM Book WHERE stock_num >= 0 " +
                "ORDER BY sale_num DESC, bid ASC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        return jdbcTemplate.queryForList(sql, offset, pageSize);
    }

    // 获取图书总数
    public int getTotalBookCount() {
        String sql = "SELECT COUNT(*) FROM Book WHERE stock_num >= 0";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    // 获取全部热销图书
    // 修改：添加cover_url字段
    public List<Map<String, Object>> getAllHotBooks() {
        String sql = "SELECT bid, book_name, author, price, stock_num, sale_num, book_type, cover_url FROM Book WHERE stock_num >= 0 ORDER BY sale_num DESC, bid ASC";
        return jdbcTemplate.queryForList(sql);
    }
}