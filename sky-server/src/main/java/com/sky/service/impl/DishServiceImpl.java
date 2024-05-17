package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
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

    @Override
    public PageResult getPage(DishPageQueryDTO dishPageQueryDTO) {
        //封装分页信息
        IPage<DishVO> page = new Page<>(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        //分页查询
        dishMapper.selectVoPage(page, dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getRecords());
    }

    @Override
    public void changStatus(Integer status, Long id) {
        //封装数据
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        //更新
        dishMapper.updateById(dish);
    }
}
