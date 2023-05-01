package com.example.restClient;

import cn.rylan.rest.annotation.HttpRequestMapping;
import cn.rylan.rest.annotation.RestHttpClient;
import cn.rylan.rest.http.HTTP;
import com.example.common.model.StudentInfo;
import com.example.common.resp.CommonReturnType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestHttpClient(serviceName = "StudentService")
public interface StudentClient {

    @HttpRequestMapping(uri = "/student/getStudentInfoById/id/{id}", type = HTTP.GET)
    public CommonReturnType getStudentInfoById(@PathVariable("id") Integer id);

    @HttpRequestMapping(uri = "/student/saveStudentInfo", type = HTTP.POST)
    public CommonReturnType saveStudentInfo(@RequestParam("name") String name, @RequestParam("password") String password);

    @HttpRequestMapping(uri = "/student/getTeacherList", type = HTTP.POST)
    public CommonReturnType getTeacherList(@RequestBody StudentInfo studentInfo);

    @HttpRequestMapping(uri = "/student/combinationTest/{scope}", type = HTTP.POST)
    public CommonReturnType combinationTest(@RequestBody StudentInfo studentInfo, @PathVariable("scope") String scope, @RequestParam("password") String password);

    @HttpRequestMapping(uri = "/student/getStudentInfoListByParam", type = HTTP.POST)
    public CommonReturnType getStudentInfoListByParam(@RequestParam("name") String name, @RequestParam("password") String password);
}
