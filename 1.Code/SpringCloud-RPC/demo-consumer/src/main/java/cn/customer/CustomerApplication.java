package cn.customer;

import cn.rylan.rest.annotation.EnableRestHttpClinet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@EnableRestHttpClinet(basePackages = "cn.customer.rest")
@SpringBootApplication
public class CustomerApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(CustomerApplication.class);
    }
}
