package cn.rylan.springboot.bean;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;


public class RestTrackInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpHeaders headers = request.getHeaders();
        // 从线程局部共享变量 TRACE_ID_THREAD_LOCAL 中获取traceId，如无则生成UUID替换
        String traceId = Optional.ofNullable(LogTrackInterceptor.getTraceId()).orElse(UUID.randomUUID().toString().replaceAll("-",""));
        // 请求头传递参数:trace-id
        headers.add("trace-id", traceId);
        // 保证请求继续被执行
        return execution.execute(request, body);
    }
}
