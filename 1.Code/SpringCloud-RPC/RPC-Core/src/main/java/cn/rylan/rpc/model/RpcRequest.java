package cn.rylan.rpc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * author: Rylan
 * create: 2022-10-24 16:09
 * desc:
 **/

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest extends RpcMessage {

    private String serviceName;

    private String interfaceName;

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] parameterValues;
}
