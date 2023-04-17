package cn.rylan.rpc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * author: Rylan
 * create: 2022-10-25 09:06
 * desc: 自定义应用层协议
 **/

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcProtocol {

    private byte[] magicNum;

    private byte version;

    private byte msgType;

    private byte serializerType;

    private int seqId;

    private Object data;
}
