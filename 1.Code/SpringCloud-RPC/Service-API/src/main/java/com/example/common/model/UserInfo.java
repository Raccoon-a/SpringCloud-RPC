package com.example.common.model;

public class UserInfo {
    private Integer id;

    private String info;

    private UserInfo() {
    }

    private UserInfo(Builder builder) {
        setId(builder.id);
        setInfo(builder.info);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }


    public static final class Builder {
        private Integer id;
        private String info;

        public Builder() {
        }

        public Builder id(Integer val) {
            id = val;
            return this;
        }

        public Builder info(String val) {
            info = val;
            return this;
        }

        public UserInfo build() {
            return new UserInfo(this);
        }
    }
}
