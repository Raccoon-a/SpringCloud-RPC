package cn.rylan.rpc.netty.constant;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * author: Rylan
 * create: 2022-10-25 14:39
 * desc: 序列化标识
 **/
@AllArgsConstructor
@NoArgsConstructor
public enum SerializerType {

    /** JSON */
    JSON((byte)0x00, "json");



    private byte flag;
    private String name;

    public byte getFlag() {
        return flag;
    }

    public String getName() {
        return name;
    }

    public static String getName(byte flag){
        for (SerializerType serializerType : SerializerType.values()) {
            if (serializerType.getFlag() == flag){
                return serializerType.getName();
            }
        }
        throw new UnsupportedOperationException("不支持该flag: " + flag);
    }

}
