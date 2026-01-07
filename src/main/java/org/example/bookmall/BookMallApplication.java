package org.example.bookmall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// 扫描mapper包下的所有Mapper接口
@MapperScan("org.example.bookmall.Mapper")
public class BookMallApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookMallApplication.class, args);
    }
}