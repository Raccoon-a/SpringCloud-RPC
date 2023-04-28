package cn.provider.controller.service.impl;

import model.PersonInfo;
import model.StudentInfo;
import org.springframework.stereotype.Service;
import service.StudentService;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl implements StudentService {

    @Override
    public PersonInfo getInfoByParam(StudentInfo studentInfo) {
        return new PersonInfo.Builder().infoId(studentInfo.getId())
                .infoBody("hello ! I'm  " + studentInfo.getStudentName() + " and I'm a student").build();
    }


    @Override
    public List<PersonInfo> getStudentListByNames(List<String> names) {
        AtomicInteger id = new AtomicInteger(1001);
        return names.stream().map(name -> {
            return new PersonInfo.Builder().infoId(id.incrementAndGet())
                    .infoBody("hello ! I'm  " + name + " and I'm a student").build();
        }).collect(Collectors.toList());
    }
}
