package cn.customer.controller;

import cn.customer.rest.HelloClient;
import cn.customer.rest.UserClient;
import cn.rylan.rpc.annotation.Remote;
import model.StudentInfo;
import model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import service.StudentService;
import service.TeacherService;
import utils.resp.CommonReturnType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class TController {

    @Value("${server.port}")
    private String port;

    @Lazy
    @Autowired
    private UserClient userClient;

    @Autowired
    private HelloClient helloClient;

    @Remote(serviceName = "provider")
    private StudentService studentService;

    @Remote(serviceName = "provider")
    private TeacherService teacherService;


    @GetMapping("/user/id/{id}")
    public CommonReturnType getUserInfo(@PathVariable("id") Integer id) {
        UserInfo userInfo = new UserInfo.Builder().name("fucker").password("123456").build();
        return CommonReturnType.create(userClient.getUserById(id) + "\n" + helloClient.hello(userInfo, "Beijing", "cao ni ma"));
    }


    @GetMapping("/getStudentInfo")
    public CommonReturnType getStudentInfo() {
        StudentInfo studentInfo = new StudentInfo.Builder().id(1001).studentName("alex").build();
        return CommonReturnType.create(studentService.getInfoByParam(studentInfo));
    }

    @GetMapping("/getStudentListInfo")
    public CommonReturnType getStudentList() {
        List<String> names = new ArrayList<>(Arrays.asList("Dave", "Eil", "Hugh", "Alma"));
        return CommonReturnType.create(studentService.getStudentListByNames(names));
    }

}
