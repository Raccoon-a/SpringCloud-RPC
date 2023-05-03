package com.example;

import cn.rylan.rest.annotation.EnableRestHttpClient;
import cn.rylan.rpc.netty.server.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;

@SpringBootApplication
@EnableRestHttpClient(basePackages = "com.example.restClient")
public class TeacherServiceApplication {




    public static void main(String[] args) {
        SpringApplication.run(TeacherServiceApplication.class, args);
    }
}
