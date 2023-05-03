package cn.rylan.springboot.bean;

import cn.rylan.rpc.annotation.Remote;
import cn.rylan.rpc.netty.client.Client;
import cn.rylan.rpc.netty.server.Server;
import cn.rylan.rpc.proxy.ObjectProxy;
import cn.rylan.springcloud.discovery.ServiceDiscovery;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ConditionalOnProperty(name = "spring.cloud.stellalou.rpc.enable", havingValue = "true")
public class Process implements BeanPostProcessor {


    private final ServiceDiscovery serviceDiscovery = SpringBeanFactory.getBean(ServiceDiscovery.class);

    private final RpcProperties rpcProperties = SpringBeanFactory.getBean(RpcProperties.class);

    public Process() {
        if (rpcProperties.getPort() != null) {
            Server.start(rpcProperties.getPort());
        }

    }


    @Override
    @SneakyThrows
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Remote.class)) {
                Remote remote = field.getAnnotation(Remote.class);
                String serviceName = remote.serviceName();
                Class<?> clazz = field.getType();
                Client client = new Client(serviceDiscovery);
                ObjectProxy proxy = new ObjectProxy(client, serviceName, clazz.getName());
                Object service = proxy.getProxy(clazz);
                field.setAccessible(true);
                try {
                    field.set(bean, service);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        return bean;

    }
}