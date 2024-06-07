package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ShoppingCartService {

    /**
     * 添加购物车
     *
     * @param shoppingCartDTO
     */
    void add(ShoppingCartDTO shoppingCartDTO);
}
