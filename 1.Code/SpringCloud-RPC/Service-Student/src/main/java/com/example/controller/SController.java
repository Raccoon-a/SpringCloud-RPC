package com.example.controller;

import com.example.common.model.StudentInfo;
import com.example.common.model.TeacherInfo;
import com.example.common.model.UserInfo;
import com.example.common.resp.CommonReturnType;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/student")
public class SController {

    private static Logger log = LoggerFactory.getLogger(SController.class);

    //路由参数
    @GetMapping("/getStudentInfoById/id/{id}")
    public CommonReturnType getStudentInfoById(@PathVariable("id") Integer id) {
        if (id == 1001) {
            StudentInfo studentInfo = new StudentInfo.Builder().id(1001).name("alex").password("123456").build();
            return CommonReturnType.create(studentInfo);
        }

        return CommonReturnType.create(null, "fail");
    }

    //路由参数
    @PostMapping("/getStudentInfoListByParam")
    public CommonReturnType getStudentInfoListByParam(@RequestParam("name") String name, @RequestParam("password") String password) {
        return CommonReturnType.create(Stream.of(
                new StudentInfo.Builder().id(1001).name(name).password(password),
                new StudentInfo.Builder().id(1001).name(name).password(password),
                new StudentInfo.Builder().id(1001).name(name).password(password),
                new StudentInfo.Builder().id(1001).name(name).password(password)
        ).collect(Collectors.toList()));
    }


    //表单参数
    @PostMapping("/saveStudentInfo")
    public CommonReturnType saveStudentInfo(@RequestParam("name") String name, @RequestParam("password") String password) {
        if (!name.isEmpty() || !password.isEmpty()) {
            return CommonReturnType.create("success");
        }

        return CommonReturnType.create("fail");
    }

    //JSON参数
    @PostMapping("/getTeacherList")
    public CommonReturnType getTeacherList(@RequestBody StudentInfo studentInfo) {
        if (studentInfo.getName().equals("alex") && studentInfo.getPassword().equals("123456")) {
            List<TeacherInfo> teacherInfoList = new ArrayList<>();
            teacherInfoList.add(new TeacherInfo.Builder().id(1001).name("John1").password("123456").build());
            teacherInfoList.add(new TeacherInfo.Builder().id(1002).name("John2").password("123456").build());
            teacherInfoList.add(new TeacherInfo.Builder().id(1003).name("John3").password("123456").build());
            teacherInfoList.add(new TeacherInfo.Builder().id(1004).name("John4").password("123456").build());

            return CommonReturnType.create(teacherInfoList);
        }
        return CommonReturnType.create(null);
    }

    //组合测试
    @PostMapping("/combinationTest/{scope}")
    public CommonReturnType combinationTest(@RequestBody StudentInfo studentInfo, @PathVariable("scope") String scope, @RequestParam("password") String password) {
        if (!studentInfo.getName().isEmpty() && !scope.isEmpty() && !password.isEmpty()) {
            String info = "In [" + scope + "] " + studentInfo.getName() + "told me his password is " + password;
            UserInfo userInfo = new UserInfo.Builder().id(1).info(info).build();
            return CommonReturnType.create(userInfo);
        }
        return CommonReturnType.create(null);
    }

}
