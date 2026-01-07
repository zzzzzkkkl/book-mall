package org.example.bookmall.controller;



//接口层，直接对接浏览器前端
//接受前端请求：把参数传给业务层，再把业务层的结果返回给前端

import org.example.bookmall.dto.BatchOrderRequest;
import org.example.bookmall.dto.OrderBookDTO;
import org.example.bookmall.service.UserService;
import org.example.bookmall.utils.ResultUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

// 1. 标记为Rest接口类 + 指定接口根路径（所有用户接口都以/user开头）
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource // 自动注入你写的UserService
    private UserService userService;


    // 1. 用户注册接口（POST请求，传参：loginName/password/phonenumber）
    @PostMapping("/register")
    public ResultUtil register(
            @RequestParam String loginName,
            @RequestParam String password,
            @RequestParam String phonenumber
    ) {
        return userService.userRegister(loginName, password, phonenumber);
    }

    // 2. 用户登录接口（POST请求，传参：loginName/password）
    @PostMapping("/login")
    public ResultUtil login(
            @RequestParam String loginName,
            @RequestParam String password,
            HttpSession session) { // 新增HttpSession参数
        // 调用Service登录
        ResultUtil result = userService.userLogin(loginName, password);
        // 登录成功时，往Session存入LOGIN_USER（和拦截器对应）
        if (result.getCode() == 200) { // ResultUtil成功的code是200
            session.setAttribute("LOGIN_USER", loginName); // 存入用户名，也可存用户ID
        }
        return result;
    }

    // 3. 查询个人信息接口（GET请求，传参：userId）
    @GetMapping("/myInfo")
    public ResultUtil getMyInfo(@RequestParam Integer userId) {
        return userService.getMyInfo(userId);
    }

    // 4. 修改个人信息接口（POST请求，传参：userId + oldPassword + newPassword + phonenumber）
    // 修复：使用@RequestBody接收JSON数据，避免参数传递问题
    @PostMapping("/updateInfo")
    public ResultUtil updateInfo(@RequestBody Map<String, String> params) {
        Integer userId = null;
        String oldPassword = "";
        String newPassword = "";
        String phonenumber = "";

        try {
            userId
                    = Integer.parseInt(params.get("userId"));
            oldPassword
                    = params.get("oldPassword") != null ? params.get("oldPassword") : "";
            newPassword
                    = params.get("newPassword") != null ? params.get("newPassword") : "";
            phonenumber
                    = params.get("phonenumber") != null ? params.get("phonenumber") : "";
        } catch (Exception e) {
            return ResultUtil.fail("参数格式错误");
        }

        return userService.updateUserInfo(userId, oldPassword, newPassword, phonenumber);
    }

    // 5. 图书搜索接口（GET请求，传参：keyword）
    @GetMapping("/searchBook")
    public ResultUtil searchBook(@RequestParam String keyword) {
        return userService.searchBook(keyword);
    }

    // 6. 创建订单接口（POST请求，传参：userId/bookId/buyNum）
    @PostMapping("/createOrder")
    public ResultUtil createOrder(
            @RequestParam Integer userId,
            @RequestParam Integer bookId,
            @RequestParam Integer buyNum
    ) {
        return userService.createOrder(userId, bookId, buyNum);
    }

    //多商品下单接口
    @PostMapping("/createBatchOrder")
    public ResultUtil createBatchOrder(
            @RequestBody BatchOrderRequest request) {
        // 从请求体中获取userId和bookList
        Integer userId = request.getUserId();
        List<OrderBookDTO> bookList = request.getBookList();
        // 调用Service层方法
        return userService.createBatchOrder(userId, bookList);
    }

    // 7. 查询个人订单接口（GET请求，传参：userId）
    @GetMapping("/getMyOrder")
    public ResultUtil getMyOrder(@RequestParam Integer userId) {
        return userService.getMyOrder(userId);
    }

    // 8. 查询订单详情接口（GET请求，传参：orderId）
    @GetMapping("/getMyOrderDetail")
    public ResultUtil getOrderDetail(@RequestParam Integer orderId) {
        return userService.getOrderDetail(orderId);
    }

    // 9. 热销榜TOP10接口（GET请求，无参数）
    @GetMapping("/getHotBooks") // 保留首页用的热销榜TOP10
    public ResultUtil getHotBooks() {
        return userService.getHotBookTop10();
    }

    // UserController.java 中添加以下方法：

    // 10. 获取所有图书（按销量排序，分页）
    @GetMapping("/getAllBooks")
    public ResultUtil getAllBooks(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer pageSize) {
        return userService.getAllBooks(page, pageSize);
    }

    // 11. 获取热销榜全部数据
    // UserController.java 中确保有以下接口
    @GetMapping("/getAllHotBooks")
    public ResultUtil getAllHotBooks() {
        return userService.getAllHotBooks();
    }

    //查询用户接口
    @GetMapping("/get")
    public ResultUtil getUser(String loginName) {
        List<Map<String, Object>> user = userService.getUserByLoginName(loginName); // 调用Service
        if (user.isEmpty()) {
            return ResultUtil.fail("用户不存在");
        }
        return ResultUtil.success(user.get(0));
    }

    // 获取图书详情接口
    // UserController.java
    @GetMapping("/getBookById")
    public ResultUtil getBookById(@RequestParam Integer bookId) {
        List<Map<String, Object>> bookList = userService.getBookById(bookId);
        if (bookList == null || bookList.isEmpty()) {
            return ResultUtil.fail("图书不存在");
        }
        // 确保返回的数据包含cover_url字段
        return ResultUtil.success(bookList.get(0));
    }

}
