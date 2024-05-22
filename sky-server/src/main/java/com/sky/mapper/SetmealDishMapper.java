package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.SetmealDish;

import java.util.List;

public interface SetmealDishMapper extends BaseMapper<SetmealDish> {
    /**
     * 批量插入
     * @param setmealDishes
     */
    void insertAll(List<SetmealDish> setmealDishes);
}
