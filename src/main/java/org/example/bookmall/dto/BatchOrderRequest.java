package org.example.bookmall.dto;


import lombok.Data;

import java.util.List;

@Data
public class BatchOrderRequest {
    private Integer userId;
    private List<OrderBookDTO> bookList;

    public BatchOrderRequest(Integer userId, List<OrderBookDTO> bookList) {
        this.userId = userId;
        this.bookList = bookList;
    }
}
