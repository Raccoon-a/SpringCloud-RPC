package com.example.controller;

import cn.rylan.rpc.annotation.Remote;
import com.example.common.model.StudentInfo;
import com.example.common.model.TeacherInfo;
import com.example.common.resp.CommonReturnType;
import com.example.common.service.TeacherService;
import com.example.restClient.StudentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UController {

    private static Logger log = LoggerFactory.getLogger(UController.class);

    @Autowired
    private StudentClient studentClient;

    @Remote(serviceName = "TeacherService")
    private TeacherService teacherService;

    @GetMapping("/studentTest")
    public CommonReturnType studentServiceTest() {
        Map<String, Object> resultMap = new HashMap<>();
        StudentInfo studentInfo = new StudentInfo.Builder().id(1001).name("alex").password("123456").build();
        resultMap.put("【路由测试结果】:        ",
                studentClient.getStudentInfoById(1001).getData());

        resultMap.put("【表单测试结果】:        ",
                studentClient.saveStudentInfo("Bob", "654321").getStatus());

        resultMap.put("【JSON测试】:           ",
                studentClient.getTeacherList(studentInfo).getData());

        resultMap.put("【组合测试结果】:         ",
                studentClient.combinationTest(studentInfo, "BEIJING", "123456").getData());

        return CommonReturnType.create(resultMap);

    }

    @GetMapping("/teacherTest")
    public CommonReturnType teacherServiceTest() {
        Map<String, Object> param = new HashMap<>();
        Map<String, Object> resultMap = new HashMap<>();
        param.put("id", 9999);
        param.put("name", "Ella");
        param.put("password", "100000");
        resultMap.put("【RPC调用单参数测试结果】:      ",
                teacherService.getTeacherInfoById(1001));

        resultMap.put("【RPC调用多参数测试结果】:      ",
                teacherService.saveTeacherInfo(new TeacherInfo.Builder().name("Gwen").password("7890").build()));

        resultMap.put("【RPC调用对象参数测试结果】:     ",
                teacherService.getTeacherInfoByParam(param));

        return CommonReturnType.create(resultMap);
    }

}
