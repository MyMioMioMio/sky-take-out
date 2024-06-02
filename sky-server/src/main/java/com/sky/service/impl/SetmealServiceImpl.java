package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealOverViewVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setMealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    @Override
    public PageResult getPage(SetmealPageQueryDTO setmealPageQueryDTO) {
        //封装页数
        IPage<SetmealVO> page = new Page<>(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        //分页查询
        setMealMapper.selectVoPage(page, setmealPageQueryDTO);
        //返回封装结果
        return new PageResult(page.getTotal(), page.getRecords());
    }

    @Override
    public void save(SetmealDTO setmealDTO) {
        //封装套餐setmeal
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //插入套餐
        setMealMapper.insert(setmeal);
        //提取setmealDish
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //判断套餐内是否有菜品
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            //遍历setmealDish赋值setmealId
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmeal.getId());
            });
            //插入全部setmealDish
            setmealDishMapper.insertAll(setmealDishes);
        }
    }

    @Override
    public void changeStatus(Integer status, Long id) {
        //判断套餐内菜品有无停售
        //提取套餐中的菜品
        List<SetmealDish> setmealDishes = setmealDishMapper.selectList(new LambdaQueryWrapper<SetmealDish>().eq(SetmealDish::getSetmealId, id));
        //提取并封装菜品id
        List<Long> dishIds = new ArrayList<>();
        setmealDishes.forEach(setmealDish -> {
            dishIds.add(setmealDish.getDishId());
        });
        //判断菜品是否停售
        Long count = dishMapper.selectCount(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getStatus, StatusConstant.DISABLE)
                .in(Dish::getId, dishIds));
        if (count > 0) {
            //套餐中存在未售商品
            throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
        }

        //封装数据
        Setmeal setmeal = Setmeal.builder().id(id).status(status).build();
        //更新
        setMealMapper.updateById(setmeal);
    }

    @Override
    public void deleteBeth(List<Long> ids) {
        //判断套餐是否启用
        Long count = setMealMapper.selectCount(new LambdaQueryWrapper<Setmeal>()
                .eq(Setmeal::getStatus, StatusConstant.ENABLE)
                .in(Setmeal::getId, ids));
        if (count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }
        //批量删除套餐
        setMealMapper.deleteBatchIds(ids);
        //批量删除套餐对应的所有菜品
        setmealDishMapper.delete(new LambdaQueryWrapper<SetmealDish>().in(SetmealDish::getSetmealId, ids));
    }

    @Override
    public void update(SetmealDTO setmealDTO) {
        //封装套餐
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //更新套餐
        setMealMapper.updateById(setmeal);
        //提取菜品信息
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //删除原有套餐-菜品关联信息
        setmealDishMapper.delete(new LambdaQueryWrapper<SetmealDish>().in(SetmealDish::getSetmealId, setmeal.getId()));
        //判断套餐内是否有菜品
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            //遍历封装SetmealId
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmeal.getId());
            });
            //添加新的套餐-菜品关联信息
            setmealDishMapper.insertAll(setmealDishes);
        }
    }

    @Override
    public SetmealVO getSetmealVO(Long id) {
        //查询套餐
        SetmealVO setmealVO = setMealMapper.selectVO(id);
        //查询菜品信息
        List<SetmealDish> setmealDishes = setmealDishMapper.selectList(new LambdaQueryWrapper<SetmealDish>().eq(SetmealDish::getSetmealId, id));
        //封装菜品信息
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    @Override
    public List<Setmeal> getSetmealList(Long categoryId) {
        return setMealMapper.selectList(new LambdaQueryWrapper<Setmeal>().eq(Setmeal::getCategoryId, categoryId));
    }

    @Override
    public List<DishItemVO> getDishItemVOListBySetmealId(Long id) {
        return setmealDishMapper.selectDishItemVOListBySetmealId(id);
    }
}
