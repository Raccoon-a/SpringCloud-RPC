package model;

public class TeacherInfo {

    private Integer id;

    private String teacherName;

    private TeacherInfo(Builder builder) {
        setId(builder.id);
        setTeacherName(builder.teacherName);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }


    public static final class Builder {
        private Integer id;
        private String teacherName;

        public Builder() {
        }

        public Builder id(Integer val) {
            id = val;
            return this;
        }

        public Builder teacherName(String val) {
            teacherName = val;
            return this;
        }

        public TeacherInfo build() {
            return new TeacherInfo(this);
        }
    }
}
