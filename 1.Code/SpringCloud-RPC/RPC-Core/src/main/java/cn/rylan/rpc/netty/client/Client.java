package cn.rylan.rpc.netty.client;


import cn.rylan.SPI.ExtensionLoad;
import cn.rylan.balancer.Balancer;
import cn.rylan.balancer.RandomBalancer;
import cn.rylan.balancer.RoundRobinBalancer;
import cn.rylan.model.ServiceInstance;
import cn.rylan.rest.springboot.bean.RestProperties;
import cn.rylan.rest.springboot.bean.SpringBeanFactory;
import cn.rylan.rpc.constant.ProtocolConstants;
import cn.rylan.rpc.constant.SerializerType;
import cn.rylan.rpc.model.RpcProtocol;
import cn.rylan.rpc.model.RpcRequest;
import cn.rylan.rpc.netty.codec.MessageFrameDecoder;
import cn.rylan.rpc.netty.codec.RpcCodec;
import cn.rylan.rpc.springboot.RpcProperties;
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
import java.util.concurrent.atomic.AtomicReference;

/**
 * author: Rylan
 * create: 2022-10-26 14:20
 * desc:
 **/
@Slf4j
public class Client {
    private final Bootstrap bootstrap;


    private static final Map<String, Map<String, Channel>> SERVICE_MAP = new ConcurrentHashMap<>();

//    private static final Map<String, Channel> CHANNEL_MAP = new ConcurrentHashMap<>();

    private static final Integer RECONNECT_TIMEOUT = 20000;

    private ServiceDiscovery serviceDiscovery;

    private String balanceType;

    private RpcProperties rpcProperties;


    private Balancer balancer = new RoundRobinBalancer();


    /**
     * 反存Channel和Service的关系,方便重连
     */
    protected static final Map<Channel, String> CHANNEL_MAP_SERVICE = new ConcurrentHashMap<>();


    protected static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);


    public Client(ServiceDiscovery serviceDiscovery) {
        rpcProperties = SpringBeanFactory.getBean(RpcProperties.class);
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
                log.info("client connected success address: {}", serviceInstance.getIp() + ":" + Integer.valueOf(serviceInstance.getPort()) + 1000);
                cf.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });
        return cf.get();
    }

    @SneakyThrows
    public Object sendRequest(RpcRequest rpcRequest) {
        Channel channel = getChannel(rpcRequest);
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

    private Channel getChannel(RpcRequest rpcRequest) {
        String serviceName = rpcRequest.getServiceName();
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
            return connect;
        } else {
            Channel channel = channelMap.get(serviceInstance.getPort());
            if (channel == null) {
                Channel connect = connect(serviceInstance);
                channelMap.put(serviceInstance.getPort(), connect);
                return connect;
            } else {
                return channel;
            }
        }
//        CHANNEL_MAP_SERVICE.put(channel, serviceName)
    }

    public void reconnect(Channel channel) {
        String serviceName = CHANNEL_MAP_SERVICE.remove(channel);
        SERVICE_MAP.remove(serviceName);
        Long start = System.currentTimeMillis();
        //延迟一秒后每三秒执行一次
        scheduledExecutorService.scheduleAtFixedRate(new ReConnectTask(start, serviceName), 1, 5, TimeUnit.SECONDS);
    }

    class ReConnectTask implements Runnable {

        private final Long start;
        private final String serviceName;

        public ReConnectTask(Long start, String serviceName) {
            this.start = start;
            this.serviceName = serviceName;
        }

        @Override
        public void run() {
            log.info("reconnecting...");
            //超过最大时常
            if (System.currentTimeMillis() - start > RECONNECT_TIMEOUT) {
                log.error("reconnect fail");
                stopTask();
            } else {
                Channel newChannel = connect(serviceDiscovery.getAllService(serviceName).get(0));
                if (newChannel != null) {
                    log.info("reconnect success");
                    CHANNEL_MAP_SERVICE.put(newChannel, serviceName);
//                    SERVICE_MAP.put(serviceName, newChannel);
                    stopTask();
                }
            }
        }

        private void stopTask() {
            scheduledExecutorService.shutdownNow();
        }
    }


}
