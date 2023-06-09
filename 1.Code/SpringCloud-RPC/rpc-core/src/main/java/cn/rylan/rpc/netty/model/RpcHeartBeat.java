package cn.rylan.rpc.netty.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * author: Rylan
 * create: 2022-10-25 15:01
 * desc:
 **/

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcHeartBeat extends RpcMessage {
    private String data;
}
