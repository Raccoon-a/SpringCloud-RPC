package service;

import model.PersonInfo;
import model.StudentInfo;

import java.util.List;

public interface StudentService {

    PersonInfo getInfoByParam(StudentInfo studentInfo);

    List<PersonInfo> getStudentListByNames(List<String> names);
}
