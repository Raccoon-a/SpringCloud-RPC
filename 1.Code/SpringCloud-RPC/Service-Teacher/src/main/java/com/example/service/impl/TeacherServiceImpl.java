package com.example.service.impl;

import com.example.common.model.StudentInfo;
import com.example.common.model.TeacherInfo;
import com.example.common.service.TeacherService;
import com.example.restClient.StudentClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TeacherServiceImpl implements TeacherService {


    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private StudentClient studentClient;


    @Override
    public TeacherInfo getTeacherInfoById(Integer id) {
        return new TeacherInfo.Builder().id(id).name("Joh").password("123456").build();
    }

    @Override
    public List<TeacherInfo> getTeacherInfoByParam(Map<String, Object> param) {
        Integer id = 1001;
        String name = "Joh";
        String password = "123456";
        if (param.get("id") != null) {
            id = (Integer) param.get("id");
        }
        if (param.get("name") != null) {
            name = (String) param.get("name");
        }
        if (param.get("password") != null) {
            password = (String) param.get("password");
        }
        return Stream.of(
                new TeacherInfo.Builder().id(id).name(name).password(password).build(),
                new TeacherInfo.Builder().id(id).name(name).password(password).build(),
                new TeacherInfo.Builder().id(id).name(name).password(password).build(),
                new TeacherInfo.Builder().id(id).name(name).password(password).build()
        ).collect(Collectors.toList());

    }

    @Override
    public int saveTeacherInfo(TeacherInfo teacherInfo) {
        return 1;
    }


}
