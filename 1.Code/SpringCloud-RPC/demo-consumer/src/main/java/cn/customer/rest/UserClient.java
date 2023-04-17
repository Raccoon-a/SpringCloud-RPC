package cn.customer.rest;

import cn.rylan.rest.annotation.RestHttpClient;
import cn.rylan.rest.annotation.HttpRequestMapping;
import cn.rylan.rest.http.HTTP;
import model.User;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

@RestHttpClient(serviceName = "provider")
public interface UserClient {

    @HttpRequestMapping(uri = "/provider/user/id/{id}", type = HTTP.GET)
    User getUserById(@PathVariable("id") Integer id);
}