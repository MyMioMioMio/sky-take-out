package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface ShoppingCartService {

    /**
     * 添加购物车
     *
     * @param shoppingCartDTO
     */
    void add(ShoppingCartDTO shoppingCartDTO);

    /**
     * 查看购物车
     * @param userId
     * @return
     */
    List<ShoppingCart> getListByUserId(Long userId);

    /**
     * 清空购物车
     * @param userId
     */
    void deleteAllByUserId(Long userId);
}
