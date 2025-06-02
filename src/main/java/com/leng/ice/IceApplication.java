package com.leng.ice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.leng.ice.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableTransactionManagement
public class IceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IceApplication.class, args);
    }
}
