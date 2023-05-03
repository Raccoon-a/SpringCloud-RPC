package cn.rylan.rpc.netty.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * author: Rylan
 * create: 2022-10-24 16:08
 * desc: 消息基类
 **/

@Data
public class RpcMessage{

    private byte msgType;

    private Integer rpcId;
}