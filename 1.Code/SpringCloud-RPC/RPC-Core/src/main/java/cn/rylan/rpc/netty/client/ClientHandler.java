package cn.rylan.rpc.netty.client;


import cn.rylan.rpc.netty.constant.ProtocolConstants;
import cn.rylan.rpc.netty.constant.SerializerType;
import cn.rylan.rpc.netty.model.RpcMessage;
import cn.rylan.rpc.netty.model.RpcProtocol;
import cn.rylan.rpc.netty.model.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * author: Rylan
 * create: 2022-10-26 14:25
 * desc:
 **/

@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<RpcMessage> {

    public static Map<Integer, Promise<Object>> PROMISE_MAP = new ConcurrentHashMap<>();

    private Client client;

    public ClientHandler(Client client) {
        this.client = client;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage) {
        if (rpcMessage.getMsgType() == ProtocolConstants.RPC_RESPONSE) {
            RpcResponse rpcResponse = (RpcResponse) rpcMessage;
            //TODO need bug fix
            Object value = rpcResponse.getReturnValue();
            Exception exception = rpcResponse.getException();
            Promise<Object> promise = PROMISE_MAP.get(rpcMessage.getRpcId());
            if (exception != null) {
                promise.setFailure(exception);
            } else {
                promise.setSuccess(value);
            }
        } else if (rpcMessage.getMsgType() == ProtocolConstants.PONG) {
            log.info("receive heartbeat [{}]", ProtocolConstants.PONG_STR);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                RpcProtocol rpcProtocol = new  RpcProtocol();
                        rpcProtocol.setMagicNum(ProtocolConstants.MAGIC_NUM);
                rpcProtocol.setVersion(ProtocolConstants.VERSION);
                rpcProtocol.setMsgType(ProtocolConstants.PING);
                rpcProtocol.setSerializerType(SerializerType.JSON.getFlag());
                rpcProtocol.setSeqId(ProtocolConstants.getSeqId());
                ctx.channel().writeAndFlush(rpcProtocol);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        client.reconnect(ctx.channel());
        super.channelInactive(ctx);
    }
}
