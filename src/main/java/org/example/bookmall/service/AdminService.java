package org.example.bookmall.service;

import org.example.bookmall.entity.Book;
import org.example.bookmall.entity.Order;
import org.example.bookmall.entity.OrderItem;
import org.example.bookmall.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 管理员业务接口（定义业务规范）
 */
public interface AdminService {

    // 1. 管理员登录
    User adminLogin(String loginName, String password);

    // 2. 新增图书 → 核心修改：返回Map
    Map<String, Object> addBook(Book book);

    // 3. 库存预警（查询stock_num≤5的图书）
    List<Book> getLowStockBooks();

    // 4. 查看所有普通用户
    List<User> listAllCommonUsers();

    // 5. 查看所有订单
    List<Order> listAllOrders();

    // 6. 修改图书库存
    boolean updateBookStock(Integer bookId, Integer newStock);

    // 7. 查看所有订单详情
    List<OrderItem> listAllOrderItems();

    // 按订单ID查询订单详情
    List<OrderItem> listOrderItemsByOrderId(Integer orderId);

    // 8. 查看所有图书（全量查询）
    List<Book> listAllBooks();

    // 9. 图书条件查询（扩展：图书ID + 作者）
    // 参数新增：Integer bookId, String author
    List<Book> listBooksByCondition(Integer bookId, String bookName, String author, String bookType, Double minPrice, Double maxPrice);

    // ========== 新增：管理员销售榜业务方法 ==========
    /**
     * 统计指定时间范围的销量数据
     * @param startTime 开始时间（默认：1970-01-01）
     * @param endTime 结束时间（默认：2100-12-31）
     * @return 销量统计结果
     */
    List<Map<String, Object>> statAdminSaleData(String startTime, String endTime);

    /**
     * 导出销量报表（支持时间筛选）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 报表数据
     */
    List<Map<String, Object>> exportAdminSaleReport(String startTime, String endTime);
    // 新增：同步销量的方法声明
    void syncBookSaleNum(List<Map<String, Object>> saleReport);

    /**
     * 按订单状态筛选销量
     * @param state 订单状态
     * @return 筛选结果
     */
    List<Map<String, Object>> statSaleByOrderState(Integer state);

    /**
     * 下架图书（stock_num设为-1）
     @param bookId 图书ID
     *@return 操作结果（true=成功，false=失败）
     */
    boolean removeBook(Integer bookId);

    // 新增：上传图书封面
    String uploadBookCover(MultipartFile file, Integer bookId) throws Exception;
    /**
     * 修改图书信息
     * @param bookId 图书ID
     * @param bookName 书名
     * @param author 作者
     * @param price 价格
     * @param bookType 分类
     * @return 操作结果
     */
    boolean updateBookInfo(Integer bookId, String bookName, String author, BigDecimal price, String bookType);
}