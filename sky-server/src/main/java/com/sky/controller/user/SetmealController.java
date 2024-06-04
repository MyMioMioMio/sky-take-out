package com.sky.controller.user;


import com.sky.constant.StatusConstant;
import com.sky.entity.Category;
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
     *
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐")
    public Result<List<Setmeal>> getList(Long categoryId) {
        log.info("根据分类id查询套餐:{}", categoryId);
        //封装套餐，仅查询启售套餐
        Setmeal setmealD = Setmeal.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        List<Setmeal> setmealList = setmealService.getSetmealList(setmealD);
        return Result.success(setmealList);
    }

    /**
     * 根据套餐id查询包含的菜品
     *
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    @ApiOperation("根据套餐id查询包含的菜品")
    public Result<List<DishItemVO>> getDish(@PathVariable("id") Long id) {
        log.info("根据套餐id查询包含的菜品:{}", id);
        //封装条件
        List<DishItemVO> dishItemVOList = setmealService.getDishItemVOListBySetmealId(id);
        return Result.success(dishItemVOList);
    }
}
