package org.example.bookmall.entity;

import java.util.Objects;

/**
 * 对应OrderItem表
 */
public class OrderItem {
    private Integer id;          // 自增主键（表中新增的id字段）
    private Integer orderId;     // 订单号（对应表中order_id）
    private Integer userId;      // 购买用户ID（对应表中user_id）
    private Integer bookId;      // 图书号（对应表中bid）
    private String bookName;     // 图书名称（对应表中book_name）
    private Integer num;         // 购买数量（对应表中num）


    // 无参构造
    public OrderItem() {
    }


    // Getter+Setter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }


    // toString
    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", userId=" + userId +
                ", bookId=" + bookId +
                ", bookName='" + bookName + '\'' +
                ", num=" + num +
                '}';
    }


    // equals+hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(id, orderItem.id) &&
                Objects.equals(orderId, orderItem.orderId) &&
                Objects.equals(userId, orderItem.userId) &&
                Objects.equals(bookId, orderItem.bookId) &&
                Objects.equals(bookName, orderItem.bookName) &&
                Objects.equals(num, orderItem.num);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orderId, userId, bookId, bookName, num);
    }
}