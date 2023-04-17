package cn.rylan.rpc.netty.codec;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;


public class RpcJsonSerializer implements Serializer {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) throws IOException {
        Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new ClassCodec()).create();
        String json = new String(bytes, StandardCharsets.UTF_8);
//        return gson.fromJson(json, clazz);
        return objectMapper.readValue(bytes, clazz);
    }

    @Override
    public <T> byte[] serialize(T object) throws JsonProcessingException {
        Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new ClassCodec()).create();
//        String json = gson.toJson(object);
        String json = objectMapper.writeValueAsString(object);
        return json.getBytes(StandardCharsets.UTF_8);
    }


    static class ClassCodec implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {

        @Override
        public Class<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            try {
                return Class.forName(jsonElement.getAsString());
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(e);
            }
        }

        @Override
        public JsonElement serialize(Class<?> clazz, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(clazz.getName());
        }
    }
}
