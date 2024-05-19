package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface DishService {

    /**
     * 新增菜品和口味
     * @param dishDTO
     */
    void addDish(DishDTO dishDTO);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult getPage(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 菜品起售、停售
     * @param status
     * @param id
     */
    void changStatus(Integer status, Long id);

    /**
     * 批量删除菜品
     * @param ids
     */
    void delete(List<Long> ids);
}
