package cn.rylan.springboot.bean;

import cn.rylan.rpc.netty.server.Server;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.cloud.stellalou.rpc")
@ConditionalOnProperty(name = "spring.cloud.stellalou.rpc.enable", havingValue = "true")
public class RpcProperties {


    private String enable;

    private Integer port;

    private Integer step = 1000;

    private String balancer = "roundRobin";
}
