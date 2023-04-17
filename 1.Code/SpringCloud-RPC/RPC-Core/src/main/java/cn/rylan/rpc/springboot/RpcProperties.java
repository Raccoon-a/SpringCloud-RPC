package cn.rylan.rpc.springboot;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.cloud.stellalou.rpc")
public class RpcProperties {
    private String enable;
    private String port;
    private String balancer = "roundRobin";
}
