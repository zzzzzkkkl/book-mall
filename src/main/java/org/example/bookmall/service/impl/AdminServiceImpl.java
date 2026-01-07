package org.example.bookmall.service.impl;

import org.example.bookmall.entity.Book;
import org.example.bookmall.entity.Order;
import org.example.bookmall.entity.OrderItem;
import org.example.bookmall.entity.User;
import org.example.bookmall.Mapper.AdminMapper;
import org.example.bookmall.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 管理员业务实现类（核心业务逻辑）
 */
@Service // 标记为业务组件，Spring自动扫描并注入Mapper
public class AdminServiceImpl implements AdminService {

    // 注入Mapper，Spring自动创建实现类
    @Autowired
    private AdminMapper adminMapper;

    // 1. 管理员登录：调用Mapper查询，返回用户对象
    @Override
    public User adminLogin(String loginName, String password) {
        // 简单参数校验（非空）
        if (loginName == null || loginName.isEmpty() || password == null || password.isEmpty()) {
            return null;
        }
        return adminMapper.selectAdminByLoginNameAndPwd(loginName, password);
    }

    // 2. 新增图书 → 核心修改：返回Map
    @Override
    public Map<String, Object> addBook(Book book) {
        Map<String, Object> resultMap = new HashMap<>();
        // 1. 参数校验
        if (book.getBookName() == null || book.getAuthor() == null || book.getPrice() == null || book.getStockNum() == null) {
            resultMap.put("success", false);
            resultMap.put("bookName", book.getBookName());
            return resultMap;
        }

        // 2. 校验是否存在相同图书
        int duplicateCount = adminMapper.countDuplicateBook(
                book.getBookName(),
                book.getAuthor(),
                book.getPrice()
        );

        if (duplicateCount > 0) {
            // 3. 存在：累加库存
            int updateRows = adminMapper.addStockToExistBook(
                    book.getBookName(),
                    book.getAuthor(),
                    book.getPrice(),
                    book.getStockNum()
            );
            if (updateRows > 0) {
                // 查询累加后的最终库存
                Integer finalStock = adminMapper.getBookCurrentStock(
                        book.getBookName(),
                        book.getAuthor(),
                        book.getPrice()
                );
                resultMap.put("success", true);
                resultMap.put("operateType", "ADD_STOCK"); // 累加库存
                resultMap.put("finalStock", finalStock);
                resultMap.put("bookName", book.getBookName());
            } else {
                resultMap.put("success", false);
                resultMap.put("bookName", book.getBookName());
            }
        } else {
            // 4. 不存在：新增图书
            int insertRows = adminMapper.insertBook(book);
            if (insertRows > 0) {
                resultMap.put("success", true);
                resultMap.put("operateType", "NEW"); // 新增图书
                resultMap.put("finalStock", book.getStockNum()); // 初始库存
                resultMap.put("bookName", book.getBookName());
            } else {
                resultMap.put("success", false);
                resultMap.put("bookName", book.getBookName());
            }
        }
        return resultMap;
    }

    // 3. 库存预警：查询stock_num≤5的图书
    @Override
    public List<Book> getLowStockBooks() {
        return adminMapper.selectLowStockBooks(5);
    }

    // 4. 查看所有普通用户
    @Override
    public List<User> listAllCommonUsers() {
        return adminMapper.selectAllCommonUsers();
    }

    // 5. 查看所有订单
    @Override
    public List<Order> listAllOrders() {
        return adminMapper.selectAllOrders();
    }

    // 6. 修改图书库存
    @Override
    public boolean updateBookStock(Integer bookId, Integer newStock) {
        // 参数校验（ID非空、库存≥0）
        if (bookId == null || newStock == null || newStock < 0) {
            return false;
        }
        return adminMapper.updateBookStock(bookId, newStock) > 0;
    }

    // 7. 查看所有订单详情
    @Override
    public List<OrderItem> listAllOrderItems() {
        return adminMapper.selectAllOrderItems();
    }
    @Override
    public List<OrderItem> listOrderItemsByOrderId(Integer orderId) {
        // 调用Mapper层的按订单ID查询方法
        return adminMapper.listOrderItemsByOrderId(orderId);
    }

    // 8. 查看所有图书（全量查询）
    @Override
    public List<Book> listAllBooks() {
        return adminMapper.selectAllBooks();
    }

    // 9. 图书条件查询（扩展：图书ID + 作者）
    @Override
    public List<Book> listBooksByCondition(Integer bookId, String bookName, String author, String bookType, Double minPrice, Double maxPrice) {
        // 价格参数转换：Double → BigDecimal
        BigDecimal minPriceBD = minPrice != null ? new BigDecimal(minPrice) : null;
        BigDecimal maxPriceBD = maxPrice != null ? new BigDecimal(maxPrice) : null;
        // 调用Mapper，传入新增的bookId和author参数
        return adminMapper.selectBooksByCondition(bookId, bookName, author, bookType, minPriceBD, maxPriceBD);
    }

    // ========== 新增：销售榜业务逻辑实现 ==========
    @Override
    public List<Map<String, Object>> statAdminSaleData(String startTime, String endTime) {
        // 修复：时间参数预处理，确保覆盖当天所有订单
        if (startTime == null || startTime.isEmpty()) {
            startTime
                    = "1970-01-01 00:00:00";  // 默认起始时间（包含所有历史数据）
        } else if (!startTime.contains(" ")) {
            startTime
                    += " 00:00:00";  // 若前端只传日期（如2026-01-02），补时分秒
        }

        if (endTime == null || endTime.isEmpty()) {
            endTime
                    = "2100-12-31 23:59:59";  // 默认结束时间
        } else if (!endTime.contains(" ")) {
            endTime
                    += " 23:59:59";  // 补时分秒，确保覆盖当天最后一秒
        }

        return adminMapper.statAdminSaleData(startTime, endTime);
    }

    @Override
    public List<Map<String, Object>> exportAdminSaleReport(String startTime, String endTime) {
        // 时间参数预处理
        if (startTime == null || startTime.isEmpty()) {
            startTime = "1970-01-01 00:00:00";
        } else if (!startTime.contains(" ")) {
            startTime += " 00:00:00";
        }

        if (endTime == null || endTime.isEmpty()) {
            endTime = "2100-12-31 23:59:59";
        } else if (!endTime.contains(" ")) {
            endTime += " 23:59:59";
        }

        // 调用Mapper，传入时间参数
        return adminMapper.exportAdminSaleReport(startTime, endTime);
    }

    // 新增：同步销量到Book表
    @Override
    public void syncBookSaleNum(List<Map<String, Object>> saleReport) {
        for (Map<String, Object> item : saleReport) {
            Integer bookId = (Integer) item.get("bookId");
            Integer totalSale = (Integer) item.get("totalSale");
            adminMapper
                    .syncBookSaleNum(bookId, totalSale);
        }
    }

    @Override
    public List<Map<String, Object>> statSaleByOrderState(Integer state) {
        // 参数校验：状态只能是0/1/2
        if (state == null || (state != 0 && state != 1 && state != 2)) {
            state = 2; // 默认统计已完成订单
        }
        return adminMapper.statSaleByOrderState(state);
    }

    // ========== 新增：下架图书业务逻辑实现 ==========
    @Override
    public boolean removeBook(Integer bookId) {
        // 1. 参数校验
        if (bookId == null) {
            return false;
        }
        // 2. 校验图书是否存在且未下架
        int existCount = adminMapper.checkBookExist(bookId);
        if (existCount == 0) {
            return false;
        }
        // 3. 执行下架：将stock_num设为-1
        int updateRows = adminMapper.logicDeleteBook(bookId);
        return updateRows > 0;
    }

    @Override
    public String uploadBookCover(MultipartFile file, Integer bookId) throws Exception {
        // 1. 校验文件
        if (file.isEmpty()) {
            throw new Exception("请选择要上传的封面图片");
        }
        // 2. 生成唯一文件名（避免重复）
        String originalFileName = file.getOriginalFilename();
        String suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
        String newFileName = UUID.randomUUID() + suffix;

        // 3. 保存图片到服务器（路径：项目根目录/upload/cover）
        String uploadPath = System.getProperty("user.dir") + "/upload/cover/";
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs(); // 自动创建文件夹
        }
        File destFile = new File(uploadPath + newFileName);
        file.transferTo(destFile);

        // 4. 生成封面访问URL（前端可直接访问）
        String coverUrl = "http://localhost:8080/upload/cover/" + newFileName;

        // 5. 更新数据库中图书的封面URL
        adminMapper.updateBookCover(bookId, coverUrl);

        return coverUrl;
    }
    @Override
    public boolean updateBookInfo(Integer bookId, String bookName, String author, BigDecimal price, String bookType) {
        // 参数校验
        if (bookId == null || bookName == null || author == null || price == null || bookType == null) {
            return false;
        }

        // 检查图书是否存在（且未下架）
        int existCount = adminMapper.checkBookExist(bookId);
        if (existCount == 0) {
            return false;
        }

        // 调用Mapper更新图书信息
        return adminMapper.updateBookInfo(bookId, bookName, author, price, bookType) > 0;
    }

}