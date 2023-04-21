package cn.rylan.rpc.springboot.bean;

import cn.rylan.rpc.annotation.Register;
import cn.rylan.rpc.annotation.Remote;
import cn.rylan.rpc.netty.client.Client;
import cn.rylan.rpc.proxy.ObjectProxy;
import cn.rylan.springcloud.discovery.ServiceDiscovery;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ConditionalOnProperty(name = "spring.cloud.stellalou.rpc.enable", havingValue = "true")
public class Process implements BeanPostProcessor {

    private final ServiceDiscovery serviceDiscovery = SpringBeanFactory.getBean(ServiceDiscovery.class);

    public static Map<String, Object> cache = new ConcurrentHashMap<>();

    public Process() {


    }


    @Override
    @SneakyThrows
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(Register.class)) {
            cache.put(bean.getClass().getSimpleName(), bean);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Remote.class)) {
                Remote remote = field.getAnnotation(Remote.class);
                String serviceName = remote.serviceName();
                Class<?> clazz = field.getType();
                Client client = new Client(serviceDiscovery);
                ObjectProxy proxy = new ObjectProxy(client, serviceName, clazz.getSimpleName());
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
}