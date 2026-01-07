package org.example.bookmall.entity;

import java.util.Objects;

/**
 * 对应[User]表
 */
public class User {
    private Integer userId;       // 用户号（对应表中user_id）
    private String loginName;     // 登录账号（对应表中login_name）
    private String password;      // 密码（对应表中password）
    private String phonenumber;   // 绑定手机号（对应表中phonenumber）
    private Integer isAdmin;      // 是否为管理员（表中新增的is_admin字段）,1表示是，0表示不是


    // 无参构造
    public User() {
    }


    // Getter+Setter
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public Integer getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Integer isAdmin) {
        this.isAdmin = isAdmin;
    }


    // toString
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", loginName='" + loginName + '\'' +
                ", password='" + password + '\'' +
                ", phonenumber='" + phonenumber + '\'' +
                ", isAdmin=" + isAdmin +
                '}';
    }


    // equals+hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId) &&
                Objects.equals(loginName, user.loginName) &&
                Objects.equals(password, user.password) &&
                Objects.equals(phonenumber, user.phonenumber) &&
                Objects.equals(isAdmin, user.isAdmin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, loginName, password, phonenumber, isAdmin);
    }
}