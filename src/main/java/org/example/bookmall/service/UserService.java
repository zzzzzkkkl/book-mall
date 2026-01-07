package org.example.bookmall.service;

import org.example.bookmall.dto.OrderBookDTO;
import org.example.bookmall.utils.ResultUtil;

import java.util.List;
import java.util.Map;

//业务方法的说明书
public interface UserService {
    // 用户注册
    ResultUtil userRegister(String loginName, String password, String phonenumber);

    // 用户登录
    ResultUtil userLogin(String loginName, String password);

    //查询用户
    // UserService.java
    public List<Map<String, Object>> getUserByLoginName(String loginName);

    //查询个人信息
    ResultUtil getMyInfo(Integer userId);

    // 修改个人信息（修改参数：增加原密码验证）
    ResultUtil updateUserInfo(Integer userId, String oldPassword, String newPassword, String phonenumber);

    // 图书模糊搜索
    ResultUtil searchBook(String keyword);

    //根据书号获得图书
    List<Map<String, Object>> getBookById(Integer bookId);

    // 创建订单
    ResultUtil createOrder(Integer userId, Integer bookId, Integer buyNum);

    // 多商品下单
    ResultUtil createBatchOrder(Integer userId, List<OrderBookDTO> bookList);

    // 查询个人订单列表
    ResultUtil getMyOrder(Integer userId);

    //查询订单详情
    ResultUtil getOrderDetail(Integer orderId);
    // 获取所有图书（分页）
    ResultUtil getAllBooks(Integer page, Integer pageSize);

    // 获取全部热销图书
    ResultUtil getAllHotBooks();

    //热销榜前十的图书
    ResultUtil getHotBookTop10();
}
