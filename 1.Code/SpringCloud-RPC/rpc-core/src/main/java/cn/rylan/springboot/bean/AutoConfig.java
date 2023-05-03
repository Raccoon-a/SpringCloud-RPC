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
package cn.rylan.springboot.bean;

import cn.rylan.rest.http.RequestInterceptor;
import cn.rylan.rest.model.RequestTemplate;
import cn.rylan.springcloud.discovery.ServiceDiscovery;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Configuration
public class AutoConfig {


    @Bean
    public ServiceDiscovery serviceDiscovery() {
        return new ServiceDiscovery(SpringBeanFactory.getBean(DiscoveryClient.class));
    }

    @Bean(name = "stellalouRestTemplate")
    @ConditionalOnProperty(name = "spring.cloud.stellalou.rest.enable", havingValue = "true")
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3600);
        requestFactory.setReadTimeout(3600);
        return new RestTemplate(requestFactory);
    }

    @Bean(name = "stellalouObjectMapper")
    @ConditionalOnProperty(name = "spring.cloud.stellalou.rest.enable", havingValue = "true")
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.cloud.stellalou.rest.trace-log", havingValue = "true")
    public LogTrackInterceptor logTrackInterceptor() {
        return new LogTrackInterceptor();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.cloud.stellalou.rest.trace-log", havingValue = "true")
    public RestTrackInterceptor restTrackInterceptor() {
        return new RestTrackInterceptor();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.cloud.stellalou.rest.trace-log", havingValue = "true")
    public WebConfig webConfig() {
        return new WebConfig();
    }


    @Bean
    @ConditionalOnProperty(name = "spring.cloud.stellalou.rest.enable", havingValue = "true")
    public RequestInterceptor interceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                var requestAttributes = RequestContextHolder.getRequestAttributes();
                assert requestAttributes != null;
                var request = ((ServletRequestAttributes) requestAttributes).getRequest();
                var cookie = request.getHeader("Cookie");
                var headers = new HttpHeaders();
                headers.add("Cookie", cookie);
                if (LogTrackInterceptor.getTraceId() != null) {
                    headers.add("trace-id", LogTrackInterceptor.getTraceId());
                }
                headers.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
                headers.setContentType(MediaType.APPLICATION_JSON);
                template.setHeaders(headers);
            }
        };
    }


//    @Bean
//    @ConditionalOnMissingBean(ZookeeperRegistration.class)
//    public ServiceInstanceRegistration serviceInstanceRegistration(
//            ApplicationContext context, ZookeeperDiscoveryProperties properties) {
//        String appName = context.getEnvironment().getProperty("spring.application.name",
//                "application");
//        String host = properties.getInstanceHost();
//        if (!StringUtils.hasText(host)) {
//            throw new IllegalStateException("instanceHost must not be empty");
//        }
//
//        properties.getMetadata().put(StatusConstants.INSTANCE_STATUS_KEY, properties.getInitialStatus());
//        //TODO 元数据
//        properties.getMetadata().put("interface", "caonima");
//
//        ZookeeperInstance zookeeperInstance = new ZookeeperInstance(context.getId(),
//                appName, properties.getMetadata());
//        ServiceInstanceRegistration.RegistrationBuilder builder = ServiceInstanceRegistration.builder().address(host)
//                .name(appName).payload(zookeeperInstance)
//                .uriSpec(properties.getUriSpec());
//
//        if (properties.getInstanceSslPort() != null) {
//            builder.sslPort(properties.getInstanceSslPort());
//        }
//        if (properties.getInstanceId() != null) {
//            builder.id(properties.getInstanceId());
//        }
//
//        return builder.build();
//    }

}
