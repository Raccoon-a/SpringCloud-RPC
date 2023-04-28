package cn.rylan.rpc.netty.codec;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * author: Rylan
 * create: 2022-10-25 15:28
 * desc: kryo序列化器
 * links: https://github.com/EsotericSoftware/kryo  [Thread safety]一节
 **/

@Slf4j
public class KryoSerializer implements Serializer {


    //多线程下安全使用kryo
    private static final ThreadLocal<Kryo> kryoLocal = new ThreadLocal<Kryo>() {
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.setReferences(true);
            kryo.setRegistrationRequired(false);
            return kryo;
        }
    };




    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {
            Kryo kryo = kryoLocal.get();
            kryo.writeObject(output, obj);
            kryoLocal.remove();
            return output.toBytes();
        } catch (Exception e) {
            log.error("序列化时有错误发生:", e);
            throw new RuntimeException("序列化时有错误发生");
        }
    }


    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) throws IOException {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = kryoLocal.get();
            Object object = kryo.readObject(input, clazz);
            kryoLocal.remove();
            return (T)object;
        } catch (Exception e) {
            log.error("反序列化时有错误发生:", e);
            throw new RuntimeException("反序列化时有错误发生");
        }
    }


}
