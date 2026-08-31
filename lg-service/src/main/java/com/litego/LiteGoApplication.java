package com.litego;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.litego.mapper")
@SpringBootApplication
public class LiteGoApplication {
    public static void main(String[] args) {
        SpringApplication.run(LiteGoApplication.class, args);
    }

}
