package com.zhuchen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@ComponentScan(basePackages = {"com.zhuchen", "com.zhuchen.controller", "com.zhuchen.service"})
public class SewageWatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(SewageWatchApplication.class, args);
    }

}
