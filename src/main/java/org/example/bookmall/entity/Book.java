package org.example.bookmall.entity;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 对应Book表
 */
public class Book {
    // 与表字段一一对应（表中是bid，这里用bookId更规范）
    private Integer bookId;       // 书号（对应表中bid）
    private String bookName;     // 书名（对应表中book_name）
    private String author;       // 作者
    private BigDecimal price;    // 价格（表中是DECIMAL，用BigDecimal避免精度丢失）
    private Integer saleNum;     // 已售数量（对应表..中sale_num）
    private Integer stockNum;    // 库存数量（对应表中stock_num）,- 1 = 下架、0 = 无货、≥1 = 有货
    private String bookType;     // 分类（对应表中book_type）
    private String coverUrl;   // 封面URL字段

    // 无参构造
    public Book() {
    }

    // Getter+Setter
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getSaleNum() {
        return saleNum;
    }

    public void setSaleNum(Integer saleNum) {
        this.saleNum = saleNum;
    }

    public Integer getStockNum() {
        return stockNum;
    }

    public void setStockNum(Integer stockNum) {
        this.stockNum = stockNum;
    }

    public String getBookType() {
        return bookType;
    }

    public void setBookType(String bookType) {
        this.bookType = bookType;
    }
    public String getCoverUrl() {
        return coverUrl;
    }
    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }
    // Book.java 需要修改toString方法
    @Override
    public String toString() {
        return "Book{" +
                "bookId=" + bookId +
                ", bookName='" + bookName + '\'' +
                ", author='" + author + '\'' +
                ", price=" + price +
                ", saleNum=" + saleNum +
                ", stockNum=" + stockNum +
                ", bookType='" + bookType + '\'' +
                ", coverUrl='" + coverUrl + '\'' +  // 添加这一行
                '}';
    }


    // 同时也需要更新equals和hashCode方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(bookId, book.bookId) &&
                Objects.equals(bookName, book.bookName) &&
                Objects.equals(author, book.author) &&
                Objects.equals(price, book.price) &&
                Objects.equals(saleNum, book.saleNum) &&
                Objects.equals(stockNum, book.stockNum) &&
                Objects.equals(bookType, book.bookType) &&
                Objects.equals(coverUrl, book.coverUrl);  // 添加这一行
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId, bookName, author, price, saleNum, stockNum, bookType, coverUrl);  // 添加coverUrl
    }
}