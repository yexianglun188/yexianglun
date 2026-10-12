package com.campus.market;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 校园二手交易平台启动类
 */
@SpringBootApplication
@MapperScan("com.campus.market.mapper")
public class CampusMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusMarketApplication.class, args);
        System.out.println("""

                ==================================================
                  校园二手交易平台 启动成功
                  接口地址: http://localhost:8080/api
                  上传目录: http://localhost:8080/uploads/
                ==================================================
                """);
    }
}
