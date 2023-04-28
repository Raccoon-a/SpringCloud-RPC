package model;

public class StudentInfo {

    private Integer id;

    private String studentName;


    private StudentInfo(Builder builder) {
        setId(builder.id);
        setStudentName(builder.studentName);
    }

    public StudentInfo() {
    }

    public StudentInfo(Integer id, String studentName) {
        this.id = id;
        this.studentName = studentName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }


    public static final class Builder {
        private Integer id;
        private String studentName;

        public Builder() {
        }

        public Builder id(Integer val) {
            id = val;
            return this;
        }

        public Builder studentName(String val) {
            studentName = val;
            return this;
        }

        public StudentInfo build() {
            return new StudentInfo(this);
        }
    }
}
