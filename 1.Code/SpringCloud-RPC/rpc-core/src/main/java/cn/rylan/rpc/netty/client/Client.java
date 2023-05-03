package cn.rylan.rpc.netty.client;


import cn.rylan.SPI.ExtensionLoad;
import cn.rylan.balancer.Balancer;
import cn.rylan.balancer.RoundRobinBalancer;
import cn.rylan.rest.model.ServiceInstance;
import cn.rylan.rpc.netty.model.ChannelRecord;
import cn.rylan.springboot.bean.SpringBeanFactory;
import cn.rylan.rpc.netty.constant.ProtocolConstants;
import cn.rylan.rpc.netty.constant.SerializerType;
import cn.rylan.rpc.netty.model.RpcProtocol;
import cn.rylan.rpc.netty.model.RpcRequest;
import cn.rylan.rpc.netty.codec.MessageFrameDecoder;
import cn.rylan.rpc.netty.codec.RpcCodec;
import cn.rylan.springboot.bean.RpcProperties;
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


    public static final Map<String, Map<String, Channel>> SERVICE_MAP = new ConcurrentHashMap<>();

    public static final Map<Channel, ChannelRecord> INSTANCE_CHANNEL_MAP = new ConcurrentHashMap<>();

    private RpcProperties rpcProperties = SpringBeanFactory.getBean(RpcProperties.class);

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
                log.info("client connected success address: {}", serviceInstance.getIp() + ":" + (Integer.valueOf(serviceInstance.getPort()) + rpcProperties.getStep()));
                cf.complete(future.channel());
            } else {
                cf.complete(null);
            }
        });
        Channel channel = cf.get();
        System.out.println(channel);
        return channel;
    }

    @SneakyThrows
    public Object sendRequest(RpcRequest rpcRequest) {
        try {
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
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
        return null;
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
            ChannelRecord channelRecord = new ChannelRecord.Builder()
                    .serviceName(serviceName).serviceInstance(serviceInstance).build();
            INSTANCE_CHANNEL_MAP.put(connect, channelRecord);
            return connect;
        } else {
            Channel channel = channelMap.get(serviceInstance.getPort());
            if (channel == null) {
                Channel connect = connect(serviceInstance);
                channelMap.put(serviceInstance.getPort(), connect);
                ChannelRecord channelRecord = new ChannelRecord.Builder()
                        .serviceName(serviceName).serviceInstance(serviceInstance).build();
                INSTANCE_CHANNEL_MAP.put(connect, channelRecord);
                return connect;
            } else {
                ChannelRecord channelRecord = new ChannelRecord.Builder()
                        .serviceName(serviceName).serviceInstance(serviceInstance).build();
                INSTANCE_CHANNEL_MAP.put(channel, channelRecord);
                return channel;
            }
        }
    }

    public void reconnect(Channel channel) {

        ChannelRecord channelRecord = INSTANCE_CHANNEL_MAP.remove(channel);
        Map<String, Channel> channelMap = SERVICE_MAP.get(channelRecord.getServiceName());
        channelMap.remove(channelRecord.getServiceInstance().getPort());
        Long start = System.currentTimeMillis();
        //延迟一秒后每三秒执行一次
        scheduledExecutorService.scheduleAtFixedRate(new ReConnectTask(start, channelRecord), 5, 3, TimeUnit.SECONDS);
    }


    class ReConnectTask implements Runnable {

        private final Long start;
        private final ChannelRecord channelRecord;

        public ReConnectTask(Long start, ChannelRecord channelRecord) {
            this.start = start;
            this.channelRecord = channelRecord;
        }

        @Override
        public void run() {

            log.info("reconnecting...");
            //超过最大时长
            if (System.currentTimeMillis() - start > RECONNECT_TIMEOUT) {
                log.error("reconnect fail");
                stopTask();
            } else {
                Channel newChannel = connect(channelRecord.getServiceInstance());
                INSTANCE_CHANNEL_MAP.put(newChannel, channelRecord);
                Map<String, Channel> channelMap = SERVICE_MAP.get(channelRecord.getServiceName());
                channelMap.put(channelRecord.getServiceInstance().getPort(), newChannel);
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
