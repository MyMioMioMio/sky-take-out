package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据时间范围查询用户数量
     * @param map
     * @return
     */
    Integer getUserCount(HashMap<Object, Object> map);
}
