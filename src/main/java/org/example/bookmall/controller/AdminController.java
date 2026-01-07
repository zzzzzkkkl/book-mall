package org.example.bookmall.controller;

import org.example.bookmall.entity.Book;
import org.example.bookmall.entity.Order;
import org.example.bookmall.entity.OrderItem;
import org.example.bookmall.entity.User;
import org.example.bookmall.service.AdminService;
import org.example.bookmall.utils.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;

/**
 * 管理员控制层（仅接收请求、调用Service、返回响应）
 */
@RestController // 标记为控制器，返回JSON数据
// 注意：这里不添加@RequestMapping("/admin")，因为现有接口已经自带/admin前缀
public class AdminController {

    // 注入Service层（Spring自动创建实例）
    @Autowired
    private AdminService adminService;

    // ========== 原有管理员接口（保持不变） ==========
    // 1. 管理员登录接口（修改后：存入Session）
    // 访问示例：http://localhost:8080/admin/login?loginName=test_admin&password=test_pwd123
    @GetMapping("/admin/login")
    public Map<String, Object> adminLogin(String loginName, String password, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        // 调用已有Service方法校验管理员
        User admin = adminService.adminLogin(loginName, password);
        if (admin != null) {
            // 登录成功：存入Session（Key=LOGIN_ADMIN，后续拦截器校验用）
            session
                    .setAttribute("LOGIN_ADMIN", admin);
            result
                    .put("success", true);
            result
                    .put("msg", "✅ 管理员登录成功");
        } else {
            // 登录失败：清空Session
            session
                    .removeAttribute("LOGIN_ADMIN");
            result
                    .put("success", false);
            result
                    .put("msg", "❌ 账号/密码错误（仅管理员可登录）");
        }
        return result;
    }
    //  新增：登录状态检查接口（前端bookManage.html依赖）
    @GetMapping("/admin/checkLogin")
    public Map<String, Object> checkLogin(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        // 仅校验Session中是否有管理员信息，返回布尔值
        result
                .put("success", session.getAttribute("LOGIN_ADMIN") != null);
        return result;
    }


    // 2. 新增图书接口 → 核心修改：解析Map结果：http://localhost:8080/admin/book/save?bookName=SpringBoot实战&author=张三&price=69.9&saleNum=0&stockNum=100&bookType=计算机
    @GetMapping("/admin/book/save")
    public String saveBook(String bookName, String author, Double price, Integer saleNum, Integer stockNum, String bookType) {
        try {
            // 参数非空校验
            if (bookName == null || author == null || price == null || stockNum == null) {
                return "❌ 新增失败（书名/作者/价格/库存不能为空）";
            }

            // 封装Book对象
            Book book = new Book();
            book.setBookName(bookName);
            book.setAuthor(author);
            book.setPrice(new BigDecimal(price));
            book.setSaleNum(saleNum == null ? 0 : saleNum);
            book.setStockNum(stockNum);
            book.setBookType(bookType);

            // 调用Service获取Map结果
            Map<String, Object> resultMap = adminService.addBook(book);
            boolean success = (boolean) resultMap.get("success");
            if (success) {
                String operateType = (String) resultMap.get("operateType");
                Integer finalStock = (Integer) resultMap.get("finalStock");
                String bookNameResult = (String) resultMap.get("bookName");

                if ("NEW".equals(operateType)) {
                    // 新增成功
                    return "✅ 图书新增成功：" + bookNameResult + "，初始库存：" + finalStock;
                } else if ("ADD_STOCK".equals(operateType)) {
                    // 累加库存成功
                    return "✅ 图书库存累加成功：" + bookNameResult + "，本次累加：" + stockNum + "，最终库存：" + finalStock;
                }
            }
            return "❌ 操作失败（参数错误或数据库异常）";
        } catch (Exception e) {
            e
                    .printStackTrace();
            return "❌ 操作失败：" + e.getMessage();
        }
    }

    // 3. 库存预警接口
    // 访问示例：http://localhost:8080/admin/stock/alert
    @GetMapping("/admin/stock/alert")
    public String stockAlert() {
        List<Book> lowStockBooks = adminService.getLowStockBooks();
        if (lowStockBooks.isEmpty()) {
            return "✅ 所有图书库存充足";
        } else {
            StringBuilder sb = new StringBuilder("⚠️ 库存预警（≤5本）：\n");
            for (Book book : lowStockBooks) {
                sb.append("书号：").append(book.getBookId())
                        .append("，书名：").append(book.getBookName())
                        .append("，库存：").append(book.getStockNum()).append("\n");
            }
            return sb.toString();
        }
    }

    // 4. 查看所有普通用户接口
    // 访问示例：http://localhost:8080/admin/user/list
    @GetMapping("/admin/user/list")
    public List<User> listAllCommonUsers() {
        return adminService.listAllCommonUsers();
    }

    // 5. 查看所有订单接口
    // 访问示例：http://localhost:8080/admin/order/list
    @GetMapping("/admin/order/list")
    public List<Order> listAllOrders() {
        return adminService.listAllOrders();
    }

    // 6. 修改图书库存接口
    // 访问示例：http://localhost:8080/admin/stock/update?bookId=1&newStock=66
    @GetMapping("/admin/stock/update")
    public String updateBookStock(Integer bookId, Integer newStock) {
        try {
            boolean success = adminService.updateBookStock(bookId, newStock);
            return success ? "✅ 书号" + bookId + "库存已更新为：" + newStock : "❌ 库存修改失败（参数错误）";
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ 修改失败：" + e.getMessage();
        }
    }

    // ========== 核心修改：合并订单详情接口（删除原有独立的listAllOrderItems方法） ==========
    /**
     * 合并版：按订单ID查询/全量查询订单详情
     * 访问示例1（全量）：http://localhost:8080/admin/order/item/list
     * 访问示例2（按ID）：http://localhost:8080/admin/order/item/list?orderId=5
     */
    @GetMapping("/admin/order/item/list")
    public List<OrderItem> listOrderItems(
            @RequestParam(required = false) Integer orderId) {
        if (orderId != null) {
            return adminService.listOrderItemsByOrderId(orderId);
        } else {
            return adminService.listAllOrderItems();
        }
    }

    /**
     * 8. 查看所有图书接口（全量查询）
     * 访问示例：http://localhost:8080/admin/book/list
     */
    @GetMapping("/admin/book/list")
    public List<Book> listAllBooks() {
        return adminService.listAllBooks();
    }

    /**9. 图书条件查询接口（扩展：图书ID + 作者）
     *按图书 ID 精准查：http://localhost:8080/admin/book/query?bookId=2
     * 按作者模糊查：http://localhost:8080/admin/book/query?author=罗
     * 图书 ID + 类型组合查：http://localhost:8080/admin/book/query?bookId=1&bookType=计算机
     * 作者 + 价格区间组合查：http://localhost:8080/admin/book/query?author=罗&minPrice=40&maxPrice=60
     */
    @GetMapping("/admin/book/query")
    public List<Book> listBooksByCondition(
            @RequestParam(required = false) Integer bookId,       // 新增：图书ID
            @RequestParam(required = false) String bookName,     // 原有：书名
            @RequestParam(required = false) String author,       // 新增：作者
            @RequestParam(required = false) String bookType,     // 原有：类型
            @RequestParam(required = false) Double minPrice,     // 原有：最低价格
            @RequestParam(required = false) Double maxPrice) {   // 原有：最高价格
        // 调用Service，传入所有参数
        return adminService.listBooksByCondition(bookId, bookName, author, bookType, minPrice, maxPrice);
    }

    // ========== 新增：管理员销售榜接口（适配现有路径格式，无类级@RequestMapping） ==========
    /*
    http://localhost:8080/admin/sale/stat          // 按时间筛选的销量排行
    http://localhost:8080/admin/sale/export        // 全量销量报表（带排行）
    http://localhost:8080/admin/sale/stat/state    // 按订单状态筛选的销量排行
     */
    /**
     * 10. 统计销量数据（按时间筛选）
     * 访问示例：http://localhost:8080/admin/sale/stat?startTime=2023-01-01&endTime=2024-12-31
     */
    @GetMapping("/admin/sale/stat")
    public Map<String, Object> statAdminSaleData(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        List<Map<String, Object>> saleData = adminService.statAdminSaleData(startTime, endTime);
        // 统一返回格式：code=200（成功），msg=提示，data=数据
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "销量统计成功");
        result.put("data", saleData);
        return result;
    }

    /**
     * 11. 导出销量报表（修改为支持时间筛选）
     * 访问示例：http://localhost:8080/admin/sale/export
     * 访问示例（带时间筛选）：http://localhost:8080/admin/sale/export?startTime=2026-01-01&endTime=2026-12-31
     */
    @GetMapping("/admin/sale/export")
    public Map<String, Object> exportAdminSaleReport(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        // 1. 按时间筛选获取销售数据
        List<Map<String, Object>> saleReport = adminService.exportAdminSaleReport(startTime, endTime);

        // 2. 同步销量到Book表
        adminService.syncBookSaleNum(saleReport);

        // 3. 重新查询（确保同步后的数据）
        List<Map<String, Object>> newSaleReport = adminService.exportAdminSaleReport(startTime, endTime);

        return new HashMap<String, Object>() {{
            put("code", 200);
            put("msg", "报表导出成功");
            put("data", newSaleReport);
            put("timeRange", startTime != null && endTime != null ?
                    startTime + " 至 " + endTime : "全部时间");
        }};
    }

    /**
     * 12. 按订单状态筛选销量
     * 访问示例：http://localhost:8080/admin/sale/stat/state?state=2
     */
    @GetMapping("/admin/sale/stat/state")
    public List<Map<String, Object>> statSaleByOrderState(
            @RequestParam(required = false) Integer state) {
        return adminService.statSaleByOrderState(state);
    }

    /**
     * 13. 下架图书接口（stock_num设为-1标记下架）
     * 访问示例：http://localhost:8080/admin/book/remove?bookId=1
     */
    @GetMapping("/admin/book/remove")
    public String removeBook(Integer bookId) {
        try {
            boolean success = adminService.removeBook(bookId);
            if (success) {
                return "✅ 书号" + bookId + "已成功下架（库存标记为-1）";
            } else {
                return "❌ 下架失败（图书不存在或已下架）";
            }
        } catch (Exception e) {
            e
                    .printStackTrace();
            return "❌ 下架失败：" + e.getMessage();
        }
    }

    // 新增：图书封面上传接口（对应前端调用）
    @PostMapping("/book/uploadCover")
    public ResultUtil uploadBookCover(
            @RequestParam("bookId") Integer bookId,
            @RequestParam("coverFile") MultipartFile coverFile) {
        try {
            String coverUrl = adminService.uploadBookCover(coverFile, bookId);
            return ResultUtil.success( coverUrl);
        } catch (Exception e) {
            return ResultUtil.fail(e.getMessage());
        }
    }
    /**
     * 修改图书信息接口
     * 访问示例：http://localhost:8080/admin/book/update?bookId=1&bookName=新书名&author=新作者&price=69.9&bookType=新分类
     */
    @PostMapping("/admin/book/update")
    public Map<String, Object> updateBookInfo(@RequestParam Integer bookId,
                                              @RequestParam String bookName,
                                              @RequestParam String author,
                                              @RequestParam Double price,
                                              @RequestParam String bookType) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 调用Service层方法
            boolean success = adminService.updateBookInfo(bookId, bookName, author, new BigDecimal(price), bookType);
            if (success) {
                result.put("code", 200);
                result.put("msg", "图书信息修改成功");
                result.put("success", true);
            } else {
                result.put("code", 400);
                result.put("msg", "图书信息修改失败");
                result.put("success", false);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "修改失败：" + e.getMessage());
            result.put("success", false);
        }
        return result;
    }
}