package org.example.bookmall.service.impl;


import org.example.bookmall.Mapper.BookOrderMapper;
import org.example.bookmall.Mapper.UserMapper;
import org.example.bookmall.dto.OrderBookDTO;
import org.example.bookmall.service.UserService;
import org.example.bookmall.utils.ResultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//业务逻辑的实现处
@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    // @Autowired注入Mapper
    // 使用构造函数注入代替字段注入（解决IDEA警告）
    private final UserMapper userMapper;
    private final BookOrderMapper bookOrderMapper;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserServiceImpl(UserMapper userMapper, BookOrderMapper bookOrderMapper, JdbcTemplate jdbcTemplate) {
        this.userMapper = userMapper;
        this.bookOrderMapper = bookOrderMapper;
        this.jdbcTemplate = jdbcTemplate;
    }
    // ----------------复用函数------------------
    //判断用户是否存在
    private ResultUtil checkUserValid(Integer userId) {
        //判断用户ID是否合法
        if (userId == null || userId <= 0) {
            return ResultUtil.fail("用户ID必须为正整数");
        }
        //调用Mapper里的getUserById，获取用户list
        //判断用户是否存在
        List<Map<String, Object>> userList = userMapper.getUserById(userId);
        if (userList == null || userList.isEmpty()) {
            return ResultUtil.fail("用户不存在，无法操作");
        }
        return null;
    }

    //判断登录名是否合法
    private ResultUtil checkLoginNameValid(String loginName) {
        //判断登录名是否为空
        if (!StringUtils.hasText(loginName)) {
            return ResultUtil.fail("登录名不能为空");
        }
        //登录名长度是否合法
        if (loginName.length() < 2 || loginName.length() > 16) {
            return ResultUtil.fail("登录名长度需在2-16位之间");
        }
        //所有校验通过，返回null表示校验成功
        return null;
    }

    //判断密码是否合法
    private ResultUtil checkPwdValid(String password) {
        //密码是否为空
        if (!StringUtils.hasText(password)) {
            return ResultUtil.fail("密码不能为空");
        }
        if (password.length() < 6 || password.length() > 16) {
            return ResultUtil.fail("密码长度需在6-16位之间");
        }
        //密码必须包含数字，字母，特殊字符
        String regex = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&*])[a-zA-Z0-9@#$%^&*]{6,16}$";
        if (!password.matches(regex)) {
            return ResultUtil.fail("密码必须包含字母、数字和特殊字符");
        }
        return null;
    }

    //判断用户绑定的手机号是否合法
    private ResultUtil checkPhoneValid(String phonenumber) {
        if (!StringUtils.hasText(phonenumber)) {
            return ResultUtil.fail("手机号不能为空");
        }
        //手机号必须首位是1，长度是11位
        String regex = "^1[3-9]\\d{9}$";
        if (!phonenumber.matches(regex)) {
            return ResultUtil.fail("请输入正确的手机号");
        }
        return null;
    }

    //检查图书库存
    private ResultUtil checkBookAndStockValid(Integer bookId, Integer buyNum) {
        logger.info("开始校验图书：bookId={}, buyNum={}", bookId, buyNum);

        if (buyNum == null || buyNum < 1) {
            return ResultUtil.fail("购买数量需大于0");
        }

        List<Map<String, Object>> bookList = this.getBookById(bookId);
        logger.info("查询图书结果：bookList={}", bookList); // 打印bookList是否为空

        if (bookList == null || bookList.isEmpty()) {
            return ResultUtil.fail("该书不存在");
        }

        Map<String, Object> book = bookList.get(0);
        logger.info("图书详情：book={}", book); // 打印book的所有字段

        // 尝试多种可能的字段名
        Object stockObj = book.get("stock_num");
        if (stockObj == null) {
            stockObj = book.get("stockNum");
        }

        logger.info("库存字段值：stockObj={}", stockObj); // 打印stock_num是否为null

        if (stockObj == null) {
            return ResultUtil.fail("该书库存信息异常");
        }

        Integer stockNum = 0;
        try {
            stockNum = Integer.parseInt(stockObj.toString());
        } catch (NumberFormatException e) {
            return ResultUtil.fail("库存格式错误");
        }

        if (stockNum < buyNum) {
            return ResultUtil.fail("库存不足，该书当前库存：" + stockNum);
        }
        return null;
    }

    //-------------------核心功能------------------------
    //查询用户
    public List<Map<String, Object>> getUserByLoginName(String loginName) {
        return userMapper.getUser(loginName); // 调用Mapper的查询方法
    }

    //1.新用户注册
    @Override
    public ResultUtil userRegister(String loginName, String password, String phonenumber) {
        try {
            // ========== ✅ 终极保障：参数强制清洗，删掉所有首尾空格 ==========
            loginName = loginName != null ? loginName.trim() : null;
            password = password != null ? password.trim() : null;
            phonenumber = phonenumber != null ? phonenumber.trim() : null;

            // 校验逻辑（用户名格式、密码格式、用户名唯一）
            ResultUtil loginNameCheck = checkLoginNameValid(loginName);
            if (loginNameCheck != null) return loginNameCheck;

            ResultUtil pwdCheck = checkPwdValid(password);
            if (pwdCheck != null) return pwdCheck;

            List<Map<String, Object>> existUser = userMapper.checkLoginNameExist(loginName);
            if (existUser != null && !existUser.isEmpty()) {
                return ResultUtil.fail("该用户名已被注册");
            }

            int addResult = userMapper.addUser(loginName, password, phonenumber, 0);
            return addResult > 0 ? ResultUtil.success("注册成功") : ResultUtil.fail("注册失败");

        } catch (Exception e) {
            if (e.getMessage().contains("Duplicate") && e.getMessage().contains("phonenumber")) {
                return ResultUtil.fail("该手机号已注册，请直接登录");
            }
            return ResultUtil.fail("注册失败：" + e.getMessage());
        }
    }

    //2.登录
    @Override
    public ResultUtil userLogin(String loginName, String password) {
        //清洗参数：登录名+密码，双保险去空格
        loginName = loginName != null ? loginName.trim() : null;
        password = password != null ? password.trim() : null;

        //判断登录名合法，不合法的返回原因
        ResultUtil res = checkLoginNameValid(loginName);
        if (res != null) return res;

        //根据用户输入的名字，密码，获取这个用户的list
        List<Map<String, Object>> userList = userMapper.checkLogin(loginName, password);
        //对比list里的内容
        if (userList == null || userList.isEmpty()) {
            return ResultUtil.fail("登录名或密码错误，请重新输入");
        }

        //登录名密码都正确
        Map<String, Object> resultData = new HashMap<>();
        //把登录成功的提示和用户的相关信息封装到Map里
        resultData.put("msg", "登录成功");
        resultData.put("userInfo", userList.get(0));
        return ResultUtil.success(resultData);
    }

    //3.查询个人信息
    @Override
    public ResultUtil getMyInfo(Integer userId) {
        //先判断用户id是否存在
        ResultUtil res = checkUserValid(userId);
        if (res != null) return res;

        //通过用户id获取用户的信息
        List<Map<String, Object>> userInfo = userMapper.getUserById(userId);
        //get（0）从List里把数据取出来，虽然只有一条
        return ResultUtil.success(userInfo.get(0));
    }

    //4.用户修改个人信息（增强版）
    @Override
    public ResultUtil updateUserInfo(Integer userId, String oldPassword, String newPassword, String phonenumber) {
        // 清洗参数，避免有空格
        if (oldPassword != null) oldPassword = oldPassword.trim();
        if (newPassword != null) newPassword = newPassword.trim();
        if (phonenumber != null) phonenumber = phonenumber.trim();

        // 校验用户ID合法性
        ResultUtil res = checkUserValid(userId);
        if (res != null) return res;

        // 验证至少修改一个字段
        boolean hasNoFieldToUpdate = !StringUtils.hasText(phonenumber) && !StringUtils.hasText(newPassword);
        if (hasNoFieldToUpdate) {
            return ResultUtil.fail("至少修改一个信息字段");
        }

        // 1. 验证原密码是否正确（必填）
        if (!StringUtils.hasText(oldPassword)) {
            return ResultUtil.fail("原密码不能为空");
        }

        // 查询用户当前信息
        List<Map<String, Object>> userList = userMapper.getUserById(userId);
        if (userList.isEmpty()) {
            return ResultUtil.fail("用户不存在");
        }

        Map<String, Object> user = userList.get(0);
        String currentPassword = (String) user.get("password");

        // 验证原密码
        if (!currentPassword.equals(oldPassword)) {
            return ResultUtil.fail("原密码错误，请重新输入");
        }

        // 2. 验证手机号（如果修改了）
        if (StringUtils.hasText(phonenumber)) {
            res
                    = checkPhoneValid(phonenumber);
            if (res != null) return res;

            // 检查手机号是否已被其他用户使用
            String checkPhoneSql = "SELECT user_id FROM [User] WHERE phonenumber = ? AND user_id != ?";
            List<Map<String, Object>> existPhone = jdbcTemplate.queryForList(checkPhoneSql, phonenumber, userId);
            if (existPhone != null && !existPhone.isEmpty()) {
                return ResultUtil.fail("该手机号已被其他用户使用");
            }
        }

        // 3. 验证新密码（如果修改了）
        if (StringUtils.hasText(newPassword)) {
            res
                    = checkPwdValid(newPassword);
            if (res != null) return res;

            // 验证新旧密码不能相同
            if (newPassword.equals(oldPassword)) {
                return ResultUtil.fail("新密码不能与原密码相同");
            }
        }

        // 执行动态更新（使用新密码参数）
        int updateCount = userMapper.updateUserInfo(userId, newPassword, phonenumber);

        // 返回结果
        return updateCount > 0 ? ResultUtil.success("信息修改成功") : ResultUtil.fail("信息修改失败，请稍后重试");
    }


    //5.搜索图书
    @Override
    public ResultUtil searchBook(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return ResultUtil.fail("搜索关键词不能为空");
        }
        //清洗参数
        keyword = keyword.trim();

        //调用Mapper里的模糊搜索 获得图书list
        List<Map<String, Object>> bookList = bookOrderMapper.searchBook(keyword);
        return bookList.isEmpty() ? ResultUtil.fail("未查询到相关图书") : ResultUtil.success(bookList);
    }

    //根据图书id获得图书
    @Override
    public List<Map<String, Object>> getBookById(Integer bookId) {
        return bookOrderMapper.getBookById(bookId);
    }

    //6.创建订单
    @Transactional// 原子操作，事务管理：确保订单创建和库存扣减要么同时成功，要么同时回滚
    @Override
    public ResultUtil createOrder(Integer userId, Integer bookId, Integer buyNum) {
        // 步骤1：校验用户合法性
        ResultUtil res = checkUserValid(userId);
        if (res != null) {
            return res;
        }
        // 步骤2：校验图书库存
        res = checkBookAndStockValid(bookId, buyNum);
        if (res != null) {
            return res;
        }

        // 步骤3：创建订单
        int orderResult = bookOrderMapper.createOrder(userId, bookId, buyNum);
        if (orderResult <= 0) {
            return ResultUtil.fail("订单创建失败");
        }

        // 步骤4：扣减图书库存（带乐观锁）
        int reduceResult = bookOrderMapper.reduceBookStock(bookId, buyNum);
        if (reduceResult <= 0) {
            // 事务会自动回滚之前的订单创建操作
            return ResultUtil.fail("图书[" + bookId + "]库存不足，下单失败");
        }

        // 所有步骤成功，返回结果
        return ResultUtil.success("订单创建成功");
    }

    //多商品下单 - 最终无BUG版 ✅ 修复所有空指针/判空/兼容问题
    @Transactional
    @Override
    public ResultUtil createBatchOrder(Integer userId, List<OrderBookDTO> bookList) {
        // 原有步骤1-6：校验用户、校验图书列表、计算总金额、创建主订单、插入订单明细（不用改）
        // ===== 1. 校验用户合法性 =====
        ResultUtil res = checkUserValid(userId);
        if (res != null) {
            return res;
        }
        // ===== 2. 校验图书列表非空 =====
        if (CollectionUtils.isEmpty(bookList)) {
            return ResultUtil.fail("请选择要购买的图书");
        }
        // ===== 3. 遍历校验每本书：库存充足+图书存在 =====
        for (OrderBookDTO bookDTO : bookList) {
            //兜底：防止单条图书数据为null
            if (bookDTO == null) {
                return ResultUtil.fail("购买的图书数据异常，存在空数据");
            }
            Integer bookId = bookDTO.getBookId();
            Integer num = bookDTO.getNum();
            // 校验图书ID/购买数量非空+合法
            if (bookId == null || bookId <= 0) {
                return ResultUtil.fail("图书ID不合法");
            }
            if (num == null || num <= 0) {
                return ResultUtil.fail("图书[" + bookId + "]购买数量必须为正整数");
            }
            // 库存+图书存在校验
            res = checkBookAndStockValid(bookId, num);
            if (res != null) {
                return res;
            }
        }
        // ===== 4. 计算订单总金额 （带完整空值兜底) =====
        BigDecimal totalPrice = new BigDecimal("0");
        for (OrderBookDTO bookDTO : bookList) {
            Integer bookId = bookDTO.getBookId();
            Integer num = bookDTO.getNum();
            List<Map<String, Object>> book = this.getBookById(bookId);
            if (book == null || book.isEmpty()) {
                return ResultUtil.fail("图书ID：" + bookId + "不存在");
            }
            Map<String, Object> bookMap = book.get(0);
            if (bookMap == null || bookMap.get("price") == null) {
                return ResultUtil.fail("图书ID：" + bookId + "价格异常，无法下单");
            }
            BigDecimal price = new BigDecimal(bookMap.get("price").toString());
            totalPrice = totalPrice.add(price.multiply(new BigDecimal(num)));
        }
        // ===== 5. 创建主订单 =====
        Integer orderId = bookOrderMapper.insertMainOrder(userId, totalPrice);
        if (orderId == null || orderId <= 0) {
            return ResultUtil.fail("主订单创建失败，未生成有效订单ID");
        }
        // ===== 6. 批量插入订单明细 =====
        for (OrderBookDTO bookDTO : bookList) {
            Integer bookId = bookDTO.getBookId();
            Integer num = bookDTO.getNum();
            int insertResult = bookOrderMapper.insertOrderItem(orderId, userId, bookId, num);
            if (insertResult <= 0) {
                return ResultUtil.fail("图书ID：" + bookId + "的订单明细插入失败");
            }
        }

        // 新增步骤7：批量扣减库存（核心修改）
        for (OrderBookDTO bookDTO : bookList) {
            Integer bookId = bookDTO.getBookId();
            Integer num = bookDTO.getNum();
            int reduceResult = bookOrderMapper.reduceBookStock(bookId, num);
            if (reduceResult <= 0) {
                // 任意一本扣减失败，事务回滚所有操作
                return ResultUtil.fail("图书[" + bookId + "]库存不足，批量下单失败");
            }
        }

        // 原有返回结果（不用改）
        return ResultUtil.success("多商品订单创建成功，订单ID：" + orderId);
    }

    //7.用户查询个人订单列表
    @Override
    public ResultUtil getMyOrder(Integer userId) {
        ResultUtil res = checkUserValid(userId);
        if (res != null) return res;

        List<Map<String, Object>> orderList = bookOrderMapper.getMyOrder(userId);
        return orderList.isEmpty() ? ResultUtil.success("暂无订单") : ResultUtil.success(orderList);
    }

    //8.用户查询订单详情
    @Override
    public ResultUtil getOrderDetail(Integer orderId) {
        //先判断订单id合法吗
        if (orderId == null || orderId <= 0) return ResultUtil.fail("订单ID为正整数");

        List<Map<String, Object>> detailList = bookOrderMapper.getOrderDetail(orderId);
        if (detailList == null || detailList.isEmpty()) {
            return ResultUtil.fail("该订单无明细数据");
        }
        return ResultUtil.success(detailList);
    }

    //9.用户查询热销榜
    @Override
    public ResultUtil getHotBookTop10() {
        // 调用Mapper里的getHotBookTop10方法
        List<Map<String, Object>> hotBookList = bookOrderMapper.getHotBookTop10();

        // 有热销数据→返回热销榜 无数据→返回默认推荐图书（按上架时间排序）
        if (hotBookList != null && !hotBookList.isEmpty()) {
            return ResultUtil.success(hotBookList);
        } else {
            // 调用图书查询方法，返回默认推荐（避免页面空白）
            List<Map<String, Object>> defaultBookList = bookOrderMapper.searchBook("");
            return ResultUtil.success(defaultBookList);
        }
    }

    // 10. 获取所有图书（按销量排序，分页）
    @Override
    public ResultUtil getAllBooks(Integer page, Integer pageSize) {
        if (page == null || page < 1) page = 1;
        if (pageSize == null || pageSize < 1) pageSize = 12;

        int offset = (page - 1) * pageSize;

        // 调用Mapper获取分页数据
        List<Map<String, Object>> bookList = bookOrderMapper.getAllBooksByPage(offset, pageSize);
        int totalCount = bookOrderMapper.getTotalBookCount();

        Map<String, Object> result = new HashMap<>();
        result.put("books", bookList);
        result.put("total", totalCount);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

        return ResultUtil.success(result);
    }

    // 11. 获取全部热销图书
    @Override
    public ResultUtil getAllHotBooks() {
        List<Map<String, Object>> hotBookList = bookOrderMapper.getAllHotBooks();
        return ResultUtil.success(hotBookList);
    }
}

