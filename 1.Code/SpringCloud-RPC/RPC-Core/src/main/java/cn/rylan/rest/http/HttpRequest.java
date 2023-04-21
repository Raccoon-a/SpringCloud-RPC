/*
 * Copyright (c) 2023 Rylan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 */
package cn.rylan.rest.http;

import cn.rylan.model.RequestTemplate;
import cn.rylan.rpc.springboot.bean.SpringBeanFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class HttpRequest {

    private ObjectMapper objectMapper = SpringBeanFactory.getBean("stellalouObjectMapper");

    private RestTemplate restTemplate = SpringBeanFactory.getBean("stellalouRestTemplate");

    private RequestInterceptor interceptor = SpringBeanFactory.getBean(RequestInterceptor.class);

    public Object get(String url, Class<?> returnType) {
        //构造请求模板
        RequestTemplate template = new RequestTemplate();
        //拦截器对请求headers进行处理
        interceptor.apply(template);
        //请求主体内容传入模板
        template.body(null);
        //从模板获取组装headers后的请求实体
        HttpEntity<String> entity = template.getEntity();
        //发送请求拿到结果
        try {
            //超时异常捕获
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (checkReturnType(returnType)) {
                return returnType.cast(response.getBody());
            }
            return objectMapper.readValue(response.getBody(), returnType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object post(String url, Object body, Class<?> returnType) throws JsonProcessingException {
        //构造请求模板
        RequestTemplate template = new RequestTemplate();
        //拦截器对请求headers进行处理
        interceptor.apply(template);
        //主体内容序列化
        String content = objectMapper.writeValueAsString(body);
        //请求主体内容传入模板
        template.body(content);
        //从模板获取组装headers后的请求实体
        HttpEntity<String> entity = template.getEntity();
        //发送请求拿到结果
        try {
            //超时异常捕获
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (checkReturnType(returnType)) {
                return returnType.cast(response.getBody());
            }
            return objectMapper.readValue(response.getBody(), returnType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean checkReturnType(Class<?> returnType) {
        return returnType.equals(String.class) || returnType.equals(Boolean.class) || returnType.equals(Character.class)
                || returnType.equals(Byte.class) || returnType.equals(Short.class) || returnType.equals(Integer.class)
                || returnType.equals(Long.class) || returnType.equals(Float.class) || returnType.equals(Double.class);
    }
}
