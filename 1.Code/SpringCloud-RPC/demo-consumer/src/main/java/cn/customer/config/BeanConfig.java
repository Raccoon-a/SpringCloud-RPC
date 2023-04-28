package cn.customer.config;

import cn.rylan.rest.http.RequestInterceptor;
import cn.rylan.rest.model.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Configuration
public class BeanConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                var requestAttributes = RequestContextHolder.getRequestAttributes();
                assert requestAttributes != null;
                var request = ((ServletRequestAttributes) requestAttributes).getRequest();
                var cookie = request.getHeader("Cookie");
                var headers = new HttpHeaders();
                headers.add("Cookie", cookie);
                headers.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
                headers.setContentType(MediaType.APPLICATION_JSON);
                template.setHeaders(headers);
            }
        };
    }
}
