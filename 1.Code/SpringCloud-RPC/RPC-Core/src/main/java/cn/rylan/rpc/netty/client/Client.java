package cn.rylan.rpc.netty.client;


import cn.rylan.SPI.ExtensionLoad;
import cn.rylan.balancer.Balancer;
import cn.rylan.balancer.RoundRobinBalancer;
import cn.rylan.model.ServiceInstance;
import cn.rylan.rpc.springboot.bean.SpringBeanFactory;
import cn.rylan.rpc.constant.ProtocolConstants;
import cn.rylan.rpc.constant.SerializerType;
import cn.rylan.rpc.model.RpcProtocol;
import cn.rylan.rpc.model.RpcRequest;
import cn.rylan.rpc.netty.codec.MessageFrameDecoder;
import cn.rylan.rpc.netty.codec.RpcCodec;
import cn.rylan.rpc.springboot.bean.RpcProperties;
import cn.rylan.springcloud.discovery.ServiceDiscovery;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * author: Rylan
 * create: 2022-10-26 14:20
 * desc:
 **/
@Slf4j
public class Client {
    private final Bootstrap bootstrap;


    private static final Map<String, Map<String, Channel>> SERVICE_MAP = new ConcurrentHashMap<>();

    private static final Map<Channel, ServiceInstance> INSTANCE_CHANNEL_MAP = new ConcurrentHashMap<>();

    private static final Integer RECONNECT_TIMEOUT = 1000000;

    private ServiceDiscovery serviceDiscovery;

    private String balanceType;

    private Balancer balancer = new RoundRobinBalancer();

    protected ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);


    public Client(ServiceDiscovery serviceDiscovery) {

        bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) {
                        nioSocketChannel.pipeline()
                                .addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS))
                                .addLast(new MessageFrameDecoder())
                                .addLast(new RpcCodec())
                                .addLast(new ClientHandler(Client.this));
                    }
                });
        this.serviceDiscovery = serviceDiscovery;
        this.balanceType = SpringBeanFactory.getBean(RpcProperties.class).getBalancer();

    }

    @SneakyThrows
    private Channel connect(ServiceInstance serviceInstance) {

        CompletableFuture<Channel> cf = new CompletableFuture<>();
        bootstrap.connect(new InetSocketAddress(serviceInstance.getIp(), Integer.valueOf(serviceInstance.getPort()) + 1000)
        ).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("client connected success address: {}", serviceInstance.getIp() + ":" + (Integer.valueOf(serviceInstance.getPort()) + 1000));
                cf.complete(future.channel());
            } else {
                cf.complete(null);
            }
        });
        return cf.get();
    }

    @SneakyThrows
    public Object sendRequest(RpcRequest rpcRequest) {
        String serviceName = rpcRequest.getServiceName();
        Channel channel = getChannel(serviceName);
        DefaultPromise<Object> promise;
        if (channel.isActive()) {
            promise = new DefaultPromise<>(channel.eventLoop());
            RpcProtocol rpcProtocol = new RpcProtocol();
            rpcProtocol.setMagicNum(ProtocolConstants.MAGIC_NUM);
            rpcProtocol.setVersion(ProtocolConstants.VERSION);
            rpcProtocol.setMsgType(ProtocolConstants.RPC_REQUEST);
            rpcProtocol.setSerializerType(SerializerType.JSON.getFlag());
            rpcProtocol.setSeqId(ProtocolConstants.getSeqId());
            rpcProtocol.setData(rpcRequest);
            ClientHandler.PROMISE_MAP.put(rpcRequest.getRpcId(), promise);
            channel.writeAndFlush(rpcProtocol);
            promise.await(3, TimeUnit.SECONDS);
            if (promise.isSuccess()) {
                return promise.getNow();
            } else {
                throw new Exception(promise.cause());
            }
        } else {
            throw new IllegalStateException();
        }
    }

    private Channel getChannel(String serviceName) throws Exception {
        Map<String, Channel> channelMap = SERVICE_MAP.get(serviceName);
        List<ServiceInstance> allService = serviceDiscovery.getAllService(serviceName);
        balancer = ExtensionLoad.getExtensionLoader(Balancer.class)
                .getExtension(balanceType, new Object[]{allService}, List.class);
        ServiceInstance serviceInstance = balancer.getInstance();
        if (channelMap == null) {
            Channel connect = connect(serviceInstance);
            Map<String, Channel> CHANNEL_MAP = new ConcurrentHashMap<>();
            CHANNEL_MAP.put(serviceInstance.getPort(), connect);
            SERVICE_MAP.put(serviceName, CHANNEL_MAP);
            INSTANCE_CHANNEL_MAP.put(connect, serviceInstance);
            return connect;
        } else {
            Channel channel = channelMap.get(serviceInstance.getPort());
            if (channel == null) {
                Channel connect = connect(serviceInstance);
                channelMap.put(serviceInstance.getPort(), connect);
                INSTANCE_CHANNEL_MAP.put(connect, serviceInstance);
                return connect;
            } else {
                INSTANCE_CHANNEL_MAP.put(channel, serviceInstance);
                return channel;
            }
        }
    }

    public void reconnect(Channel channel) {

        ServiceInstance serviceInstance = INSTANCE_CHANNEL_MAP.remove(channel);
        Long start = System.currentTimeMillis();
        //延迟一秒后每三秒执行一次
        scheduledExecutorService.scheduleAtFixedRate(new ReConnectTask(start, serviceInstance), 5, 3, TimeUnit.SECONDS);
    }


    class ReConnectTask implements Runnable {

        private final Long start;
        private final ServiceInstance serviceInstance;

        public ReConnectTask(Long start, ServiceInstance serviceInstance) {
            this.start = start;
            this.serviceInstance = serviceInstance;
        }

        @Override
        public void run() {

            log.info("reconnecting...");
            //超过最大时常
            if (System.currentTimeMillis() - start > RECONNECT_TIMEOUT) {
                log.error("reconnect fail");
                stopTask();
            } else {
                Channel newChannel = connect(serviceInstance);
                if (newChannel != null) {
                    log.info("reconnect success，scheduledExecutorService关闭");
                    stopTask();
                }
            }
        }

        private void stopTask() {
            scheduledExecutorService.shutdownNow();
        }
    }


}
