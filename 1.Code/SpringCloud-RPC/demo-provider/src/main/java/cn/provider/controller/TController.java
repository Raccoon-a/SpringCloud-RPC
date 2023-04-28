package cn.provider.controller;


import model.UserInfo;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;

@RestController
public class TController {

    @GetMapping("/provider/user/id/{id}")
    public UserInfo getUserById(@PathVariable("id") Integer id) {
        if (id != 1001) {
            return null;
        }
        return new UserInfo(1001, "alex", "123456");
    }


    // url : 127.0.0.1:4000/provider/BEIJING/hello?msg=cao ni ma
    @PostMapping("/provider/{scope}/hello")
    public String hello(@RequestBody UserInfo userInfo, @PathVariable("scope") String scope, @PathParam("msg") String msg) {
        return "【In " + scope + "】: " + userInfo.getName() + " told me " + msg + " and then told me his password is " + userInfo.getPassword();
    }
}
