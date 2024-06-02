package com.sky.controller.user;


import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Slf4j
@Api(tags = "C端-套餐浏览接口")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据分类id查询套餐
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐")
    public Result<List<Setmeal>> getList(Long categoryId) {
        log.info("根据分类id查询套餐:{}", categoryId);
        List<Setmeal> setmealList = setmealService.getSetmealList(categoryId);
        return Result.success(setmealList);
    }

    /**
     * 根据套餐id查询包含的菜品
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    @ApiOperation("根据套餐id查询包含的菜品")
    public Result<List<DishItemVO>> getDish(@PathVariable("id") Long id) {
        log.info("根据套餐id查询包含的菜品:{}", id);
        List<DishItemVO> dishItemVOList = setmealService.getDishItemVOListBySetmealId(id);
        return Result.success(dishItemVOList);
    }
}
