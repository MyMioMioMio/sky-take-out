package com.sky.service;

import com.sky.entity.User;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface UserService {

    /**
     * 登录
     * @param code 微信授权码
     * @return
     */
    User wxLogin(String code);
}
