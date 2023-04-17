package cn.customer.rest;

import cn.rylan.rest.annotation.HttpRequestMapping;
import cn.rylan.rest.annotation.RestHttpClient;
import cn.rylan.rest.http.HTTP;
import model.User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.websocket.server.PathParam;

@RestHttpClient(serviceName = "provider")
public interface HelloClient {

    //组合测试
    @HttpRequestMapping(uri = "/provider/{scope}/hello", type = HTTP.POST)
    String hello(@RequestBody User user, @PathVariable("scope") String scope, @PathParam("msg") String msg);
}
