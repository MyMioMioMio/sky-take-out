package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Override
    public void addDish(DishDTO dishDTO) {
        //封装菜品数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //新增菜品
        dishMapper.insert(dish);
        Long id = dish.getId();
        //提取口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        //判断是否有口味
        if (flavors != null && !flavors.isEmpty()) {
            //遍历给dishId赋值
            flavors.forEach(flavor -> flavor.setDishId(id));
            //保存口味
            dishFlavorMapper.insertAll(flavors);
        }

    }
}
