package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.SetmealDish;
import com.sky.vo.DishItemVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SetmealDishMapper extends BaseMapper<SetmealDish> {
    /**
     * 批量插入
     * @param setmealDishes
     */
    void insertAll(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐id查询包含的菜品
     * @param id
     * @return
     */
    List<DishItemVO> selectDishItemVOListBySetmealId(@Param("setmealId") Long id);
}
