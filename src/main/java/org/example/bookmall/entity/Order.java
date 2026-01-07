package org.example.bookmall.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

/**
 * 对应[Order]表
 */
public class Order {
    private Integer orderId;      // 订单号（对应表中order_id）
    private Integer userId;       // 购买用户ID（对应表中user_id）
    private Date saleTime;        // 下单时间（表中是DATETIME，用Date类型）
    private Integer state;        // 订单状态（对应表中state）-- 订单状态：0-删除，1-正常（待支付），2-交易（已完成）
    private BigDecimal salePrice; // 总金额（表中是DECIMAL，用BigDecimal）


    // 无参构造
    public Order() {
    }


    // Getter+Setter
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

    public Date getSaleTime() {
        return saleTime;
    }

    public void setSaleTime(Date saleTime) {
        this.saleTime = saleTime;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }


    // toString
    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", userId=" + userId +
                ", saleTime=" + saleTime +
                ", state=" + state +
                ", salePrice=" + salePrice +
                '}';
    }


    // equals+hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(orderId, order.orderId) &&
                Objects.equals(userId, order.userId) &&
                Objects.equals(saleTime, order.saleTime) &&
                Objects.equals(state, order.state) &&
                Objects.equals(salePrice, order.salePrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, userId, saleTime, state, salePrice);
    }
}