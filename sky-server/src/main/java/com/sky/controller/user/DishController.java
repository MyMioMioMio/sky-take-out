package com.sky.controller.user;

import com.sky.constant.CacheConstant;
import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api("C端-菜品浏览接口")
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    @Cacheable(cacheNames = CacheConstant.CACHE_KEY_DISH_PREFIX, key = "#categoryId")
    public Result<List<DishVO>> getListById(Long categoryId) {
        log.info("根据分类id查询菜品:{}", categoryId);
        //封装dish条件,仅查询启售中的
        Dish dishD = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();

        List<DishVO> dishVOList = dishService.getDishVOListByCategoryId(dishD);
        return Result.success(dishVOList);
    }
}
