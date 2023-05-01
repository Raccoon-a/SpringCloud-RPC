package com.example;

import cn.rylan.rest.annotation.EnableRestHttpClient;
import cn.rylan.rpc.netty.server.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRestHttpClient(basePackages = "com.example.restClient")
public class TeacherServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TeacherServiceApplication.class);
        Server.start(7000);
    }
}
