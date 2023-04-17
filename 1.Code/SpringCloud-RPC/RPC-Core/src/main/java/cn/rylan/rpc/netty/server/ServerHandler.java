package cn.rylan.rpc.netty.server;


import cn.rylan.rest.springboot.bean.SpringBeanFactory;
import cn.rylan.rpc.constant.ProtocolConstants;
import cn.rylan.rpc.constant.SerializerCode;
import cn.rylan.rpc.model.RpcMessage;
import cn.rylan.rpc.model.RpcProtocol;
import cn.rylan.rpc.model.RpcRequest;
import cn.rylan.rpc.model.RpcResponse;
//import cn.rylan.rpc.springboot.Process;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;


/**
 * author: Rylan
 * create: 2022-10-27 14:33
 * desc:
 **/

@Slf4j
public class ServerHandler extends SimpleChannelInboundHandler<RpcMessage> {


    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Description：监听读事件判断根据接收到的数据类型响应
     *
     * @param ctx        the {@link ChannelHandlerContext} which this {@link SimpleChannelInboundHandler}
     *                   belongs to
     * @param rpcMessage the message to handle
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage rpcMessage) throws Exception {
        RpcProtocol protocol = new RpcProtocol();
        protocol.setMagicNum(ProtocolConstants.MAGIC_NUM);
        protocol.setVersion(ProtocolConstants.VERSION);
        protocol.setSerializerType(SerializerCode.KRYO);
        protocol.setSeqId(ProtocolConstants.getSeqId());
        //TODO 如果接收到的是心跳检测数据包
        if (rpcMessage.getMsgType() == ProtocolConstants.PING) {
            protocol.setMsgType(ProtocolConstants.PONG);
            log.info("receive heartbeat [{}]", ProtocolConstants.PING_STR);
            ctx.channel().writeAndFlush(protocol);
        } else if (rpcMessage.getMsgType() == ProtocolConstants.RPC_REQUEST) {
            RpcRequest rpcRequest = (RpcRequest) rpcMessage;
            protocol.setMsgType(ProtocolConstants.RPC_RESPONSE);
            protocol.setData(invokeMethod(rpcRequest));
            ctx.channel().writeAndFlush(protocol);
        }
    }

    private String captureName(String name) {
        name = name.substring(0, 1).toLowerCase() + name.substring(1);//UpperCase大写
        return name;

    }

    /**
     * Description：执行目标方法
     *
     * @param rpcRequest
     * @return
     */
    public Object invokeMethod(RpcRequest rpcRequest) throws JsonProcessingException {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRpcId(rpcRequest.getRpcId());
        Object[] parameterValues = rpcRequest.getParameterValues();
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] params = new Object[parameterTypes.length];
        for (int i = 0; i < parameterValues.length; i++) {
            String value = (String) parameterValues[i];
            Class<?> type = parameterTypes[i];
            Object object = objectMapper.readValue(value, type);
            params[i] = object;
        }
        try {
            String methodName = rpcRequest.getMethodName();
            String interfaceName = captureName(rpcRequest.getInterfaceName());
            Object bean = SpringBeanFactory.getBean(interfaceName);
            Object result = bean.getClass().getDeclaredMethod(methodName,parameterTypes).invoke(bean,params);
            rpcResponse.setReturnValue(result);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("[{}]方法执行失败,原因: {}", rpcRequest.getMethodName(), e.getCause().getMessage());
            rpcResponse.setException(new Exception(e.getCause().getMessage()));
        }
        return rpcResponse;
    }

}
