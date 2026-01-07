package org.example.bookmall.dto;

public class OrderBookDTO {
    // 图书bid（对应Book表的bid）
    private Integer bookId;
    // 购买数量
    private Integer num;

    //无参构造方法（Spring接收JSON参数必须）
    public OrderBookDTO() {
    }

    public OrderBookDTO(Integer bookId, Integer num) {
        this.bookId = bookId;
        this.num = num;
    }

    //getter/setter方法
    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }
}

