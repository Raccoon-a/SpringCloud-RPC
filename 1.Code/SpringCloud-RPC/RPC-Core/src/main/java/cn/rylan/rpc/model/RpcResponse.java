package cn.rylan.rpc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * author: Rylan
 * create: 2022-10-25 12:04
 * desc:
 **/

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse extends RpcMessage {

    private Object returnValue;

    private Exception exception;
}
