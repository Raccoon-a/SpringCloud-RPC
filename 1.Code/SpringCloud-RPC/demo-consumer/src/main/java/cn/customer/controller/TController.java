package cn.customer.controller;

import cn.customer.rest.HelloClient;
import cn.customer.rest.UserClient;
import cn.customer.service.StudentService;
import cn.customer.service.TeacherService;
import cn.rylan.rpc.annotation.Remote;
import model.User;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class TController {

    @Lazy
    @Autowired
    private UserClient userClient;

    @Autowired
    private HelloClient helloClient;

    @Remote(serviceName = "provider")
    private StudentService studentService;

    @Remote(serviceName = "provider")
    private TeacherService teacherService;


    @GetMapping("/customer/user/id/{id}")
    public String getUserInfo(@PathVariable("id") Integer id) throws IOException {
        User user = new User();
        user.setName("fucker");
        user.setPassword("123456");
        return userClient.getUserById(id) + "\n" + helloClient.hello(user, "Beijing", "caonima");
    }


    @GetMapping("/test")
    public String getStudentInfo() {
        String info0 = studentService.getStudent(1003);
//        String info1 = teacherService.getStudent(1001);
//        System.out.println(info0);
//        System.out.println(info1);
        return info0+"  ";
    }

}
