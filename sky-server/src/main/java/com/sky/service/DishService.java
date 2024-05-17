package com.sky.service;

import com.sky.dto.DishDTO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface DishService {

    /**
     * 新增菜品和口味
     * @param dishDTO
     */
    void addDish(DishDTO dishDTO);
}
