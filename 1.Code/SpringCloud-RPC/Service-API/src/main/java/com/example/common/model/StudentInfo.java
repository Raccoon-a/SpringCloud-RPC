package com.example.common.model;

public class StudentInfo {

    private Integer id;

    private String name;

    private String password;

    public StudentInfo() {
    }

    private StudentInfo(Builder builder) {
        setId(builder.id);
        setName(builder.name);
        setPassword(builder.password);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public static final class Builder {
        private Integer id;
        private String name;
        private String password;

        public Builder() {
        }

        public Builder id(Integer val) {
            id = val;
            return this;
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder password(String val) {
            password = val;
            return this;
        }

        public StudentInfo build() {
            return new StudentInfo(this);
        }
    }
}
