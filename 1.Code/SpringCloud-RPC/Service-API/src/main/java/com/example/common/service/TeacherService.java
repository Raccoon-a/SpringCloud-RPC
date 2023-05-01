package com.example.common.service;

import com.example.common.model.StudentInfo;
import com.example.common.model.TeacherInfo;

import java.util.List;
import java.util.Map;

public interface TeacherService {

    TeacherInfo getTeacherInfoById(Integer id);

    List<TeacherInfo> getTeacherInfoByParam(Map<String, Object> param);

    int saveTeacherInfo(TeacherInfo teacherInfo);



}
