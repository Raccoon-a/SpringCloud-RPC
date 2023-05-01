package com.example.common.service;

import com.example.common.model.UserInfo;

import java.util.Map;

public interface UserService {

    UserInfo getUserInfoByParam(Map<String, Object> param);
}
