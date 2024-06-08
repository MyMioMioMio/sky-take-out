package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;


    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        //封装shoppingCart
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        //判断是否存在shoppingCart
        ShoppingCart shoppingCartGet = shoppingCartMapper.selectOne(
                new LambdaQueryWrapper<ShoppingCart>()
                        .eq(shoppingCart.getDishId() != null, ShoppingCart::getDishId, shoppingCart.getDishId())
                        .eq(shoppingCart.getDishFlavor() != null, ShoppingCart::getDishFlavor, shoppingCart.getDishFlavor())
                        .eq(shoppingCart.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCart.getSetmealId())
        );
        if (shoppingCartGet != null) {
            //已经存在shoppingCart
            //数量加1
            shoppingCartGet.setNumber(shoppingCartGet.getNumber() + 1);
            shoppingCartMapper.updateById(shoppingCartGet);
        } else {
            //不存在shoppingCart,则新增shoppingCart
            if (shoppingCart.getDishId() != null) {
                //shoppingCart为菜品，查询菜品信息
                Dish dish = dishMapper.selectById(shoppingCart.getDishId());
                //设置shoppingCart信息
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }else {
                //shoppingCart为套餐，查询套餐信息
                Setmeal setmeal = setmealMapper.selectById(shoppingCart.getSetmealId());
                //设置shoppingCart信息
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setUserId(BaseContext.getCurrentId());
            //新增购物车商品
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    @Override
    public List<ShoppingCart> getListByUserId(Long userId) {
        return shoppingCartMapper.selectList(new LambdaQueryWrapper<ShoppingCart>().eq(ShoppingCart::getUserId, userId));
    }
}
