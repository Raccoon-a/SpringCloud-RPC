package cn.rylan.rpc.springboot.bean;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.cloud.stellalou.rpc")
@ConditionalOnProperty(name = "spring.cloud.stellalou.rpc.enable", havingValue = "true")
public class RpcProperties {
    private String enable;
    private String port;
    private String balancer = "roundRobin";
}
