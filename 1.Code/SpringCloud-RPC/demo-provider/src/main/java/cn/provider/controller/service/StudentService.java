package cn.provider.controller.service;


import cn.rylan.rpc.annotation.Register;

@Register(serviceName = "provider")
public class StudentService {

    public String getStudent(Integer id) {
        System.out.println("t");
        return "1001";
    }
}
