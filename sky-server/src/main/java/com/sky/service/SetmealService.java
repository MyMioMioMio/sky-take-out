package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface SetmealService {

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult getPage(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 新增套餐
     * @param setmealDTO
     */
    void save(SetmealDTO setmealDTO);

    /**
     * 套餐起售、停售
     * @param status
     * @param id
     */
    void changeStatus(Integer status, Long id);

    /**
     * 批量删除套餐
     * @param ids
     */
    void deleteBeth(List<Long> ids);

    /**
     * 修改套餐
     * @param setmealDTO
     */
    void update(SetmealDTO setmealDTO);

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    SetmealVO getSetmealVO(Long id);

    /**
     * 根据分类id查询套餐
     * @param setmealD
     * @return
     */
    List<Setmeal> getSetmealList(Setmeal setmealD);

    /**
     * 根据套餐id查询包含的菜品
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemVOListBySetmealId(Long id);
}
