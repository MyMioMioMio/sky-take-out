package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.constant.MessageConstant;
import com.sky.constant.WXLoginStringConstant;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    WeChatProperties weChatProperties;

    //微信登录接口
    public static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    public static final String GRANT_TYPE = "authorization_code";

    public static final String OPENID = "openid";


    @Override
    public User wxLogin(String code) {
        //微信登录获取openId
        String openId = getOpenId(code);
        //openid为空，则抛登录业务异常
        if (openId == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //查询用户是否存在
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, openId));
        if (user == null) {
            //不存在则自动注册
            user = User.builder()
                    .openid(openId)
                    .build();
            userMapper.insert(user);
        }
        //返回用户信息
        return user;
    }

    /**
     * 获取openId
     * @param code
     * @return
     */
    private String getOpenId(String code) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(WXLoginStringConstant.APPID, weChatProperties.getAppid());
        paramMap.put(WXLoginStringConstant.SECRET, weChatProperties.getSecret());
        paramMap.put(WXLoginStringConstant.JS_CODE, code);
        paramMap.put(WXLoginStringConstant.GRANT_TYPE, GRANT_TYPE);
        String jsonString = HttpClientUtil.doGet(WX_LOGIN_URL, paramMap);
        JSONObject jsonObject = JSON.parseObject(jsonString);
        return jsonObject.getString(OPENID);
    }
}
