package cn.provider.controller.service;


import cn.rylan.rpc.annotation.Register;

@Register(serviceName = "provider")
public class TeacherService {

    public String getStudent(Integer id) {
        return "1003";
    }
}
