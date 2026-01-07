package org.example.bookmall.Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//用户相关的sql语句，Service层来调用
@Repository//标记这是数据库操作类，Spring自动管理
public class UserMapper {
    @Autowired//Spring的自动注入注解：自动创建JdbcTemplate对象，不用手动new一个
    //JdbcTemplate：连接数据库、执行SQL语句、获取结果，而我们只需调用他的update()(执行增删改查)，
    //queryForList()(执行查询)方法即可
    private JdbcTemplate jdbcTemplate;

    //SQL:查询用户
    public List<Map<String, Object>> getUser(String loginName) {
        String sql = "SELECT * FROM [User] WHERE login_name=?";
        // 用JdbcTemplate查询，自动映射到User实体
        return jdbcTemplate.queryForList(sql, loginName);
    }

    //SQL:新增用户（注册）
    //注意：
    //user_id是自增主键，这里无需手动插入
    public int addUser(String loginName, String password, String phonenumber, Integer isAdmin) {
        String sql = "INSERT INTO[User](login_name,password,phonenumber,is_admin) VALUES(?,?,?,?)";
        //返回受影响的行数，1：成功，0：失败
        return jdbcTemplate.update(sql, loginName, password, phonenumber, isAdmin);
    }

    //SQL：用户登录校验
    public List<Map<String, Object>> checkLogin(String loginName, String password) {
        String sql = "SELECT * FROM[User] WHERE login_name=? AND password=? AND is_admin=0";
        //返回符合条件的列表。校验成功：List里有一个Map对象，失败：List为空
        // sql查询到的数据转换成Map，在把所有行放到List里。
        return jdbcTemplate.queryForList(sql, loginName, password);
    }

    //SQL：检查登录名是否存在
    public List<Map<String, Object>> checkLoginNameExist(String loginName) {
        String sql = "SELECT user_id FROM [User] WHERE login_name = ?";
        return jdbcTemplate.queryForList(sql, loginName);
    }

    //SQL：用户修改个人信息
    //动态更新个人信息，用户想修改哪个修改哪个
    //SQL：用户修改个人信息
    //SQL：用户修改个人信息（简化版，只更新密码和手机号）
    public int updateUserInfo(Integer userId, String password, String phonenumber) {
        StringBuilder sql = new StringBuilder("UPDATE [User] SET ");
        List<Object> params = new ArrayList<>();

        if (StringUtils.hasText(password)) {
            sql.append("password = ?, ");
            params.add(password);
        }
        if (StringUtils.hasText(phonenumber)) {
            sql.append("phonenumber = ?, ");
            params.add(phonenumber);
        }

        // 无字段更新，直接返回
        if (params.isEmpty()) return 0;

        // 移除最后一个逗号
        String sqlStr = sql.toString();
        if (sqlStr.endsWith(", ")) {
            sqlStr = sqlStr.substring(0, sqlStr.length() - 2);
        }

        // 拼接WHERE条件
        sqlStr += " WHERE user_id = ?";
        params.add(userId);

        return jdbcTemplate.update(sqlStr, params.toArray());
    }

    //SQL:用户查询个人信息
    //数据库里的记录：列名→值，对应Map里key→value
    public List<Map<String, Object>> getUserById(Integer userId) {
        String sql = "SELECT * FROM[User] WHERE user_id=?";
        return jdbcTemplate.queryForList(sql, userId);
    }
}
