package cn.rylan.rpc.netty.codec;


import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;

public interface Serializer {

    /**
     * Description: 反序列化
     * @param clazz
     * @param bytes
     * @return T
     * Author: ink
     * Date: 2022/2/28
    */
    <T> T deserialize(Class<T> clazz, byte[] bytes) throws IOException;

    /**
     * Description: 序列化
     * @param object
     * @return byte[]
     * Author: ink
     * Date: 2022/2/28
    */
    <T> byte[] serialize(T object) throws JsonProcessingException;
}
