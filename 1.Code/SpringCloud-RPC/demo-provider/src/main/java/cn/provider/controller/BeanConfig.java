package cn.provider.controller;

import cn.rylan.rpc.netty.server.Server;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class BeanConfig implements ApplicationRunner {

    @Value("${server.port}")
    private String port;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        Server.start(Integer.valueOf(port) + 1000);
    }
}
