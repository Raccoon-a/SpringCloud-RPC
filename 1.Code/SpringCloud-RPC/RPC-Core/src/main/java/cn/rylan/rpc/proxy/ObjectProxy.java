package cn.rylan.rpc.proxy;

import cn.rylan.rpc.model.RpcRequest;
import cn.rylan.rpc.netty.client.Client;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * author: Rylan
 * create: 2022-10-25 12:28
 * desc: Cglib动态代理
 * links: https://blog.csdn.net/yaomingyang/article/details/82762697
 **/

public class ObjectProxy implements MethodInterceptor {


    private static final Map<Class<?>, Object> PROXY_MAP = new ConcurrentHashMap<>();
    private final Client client;
    private final String serviceName;
    private final String interfaceName;
    private static final AtomicInteger RPC_ID = new AtomicInteger(10000);
    private ObjectMapper objectMapper  = new ObjectMapper();

    public ObjectProxy(Client client, String serviceName, String interfaceName) {
        this.client = client;
        this.serviceName = serviceName;
        this.interfaceName = interfaceName;
    }


    public Object createProxy(Class<?> clazz) {
        //创建动态代理增强类
        Enhancer enhancer = new Enhancer();
        //设置类加载器
        enhancer.setClassLoader(clazz.getClassLoader());
        //设置被代理类
        enhancer.setSuperclass(clazz);
        //拦截触发回调执行intercept
        enhancer.setCallback(this);
        return enhancer.create();
    }


    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) PROXY_MAP.computeIfAbsent(clazz, (this::createProxy));
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {

        Object[] params = new Object[objects.length];
        for (int i = 0; i < objects.length; i++) {
            params[i] = objectMapper.writeValueAsString(objects[i]);
        }


        //TODO Client发送请求
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setMethodName(method.getName());
        rpcRequest.setServiceName(serviceName);
        rpcRequest.setInterfaceName(interfaceName);
        rpcRequest.setParameterTypes(method.getParameterTypes());
        rpcRequest.setParameterValues(params);
        rpcRequest.setRpcId(RPC_ID.getAndIncrement());
        return client.sendRequest(rpcRequest);
    }
}
