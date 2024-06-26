package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private SetmealService setmealService;

    @Override
    public void addDish(DishDTO dishDTO) {
        //封装菜品数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //新增菜品
        dishMapper.insert(dish);
        Long id = dish.getId();
        //提取口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        //判断是否有口味
        if (flavors != null && !flavors.isEmpty()) {
            //遍历给dishId赋值
            flavors.forEach(flavor -> flavor.setDishId(id));
            //保存口味
            dishFlavorMapper.insertAll(flavors);
        }
    }

    @Override
    public PageResult getPage(DishPageQueryDTO dishPageQueryDTO) {
        //封装分页信息
        IPage<DishVO> page = new Page<>(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        //分页查询
        dishMapper.selectVoPage(page, dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getRecords());
    }

    @Override
    public void changStatus(Integer status, Long id) {
        //如果执行停售操作，则包含此菜品的套餐也需要停售
        if (status.intValue() == StatusConstant.DISABLE) {
            //根据菜品id查询套餐-菜品
            List<SetmealDish> setmealDishes = setmealDishMapper.selectList(new LambdaQueryWrapper<SetmealDish>().eq(SetmealDish::getDishId, id));
            //遍历更改套餐状态为停售
            setmealDishes.forEach(setmealDish -> {
                setmealService.changeStatus(StatusConstant.DISABLE, setmealDish.getSetmealId());
            });
        }

        //封装数据
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        //更新
        dishMapper.updateById(dish);
    }

    @Override
    public void delete(List<Long> ids) {
        //判断是否存在启售中的菜品
        List<Dish> dishes = dishMapper.selectBatchIds(ids);
        dishes.forEach(dish -> {
            if (dish.getStatus().intValue() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        });
        //判断是否存在有套餐关联的菜品
        LambdaQueryWrapper<SetmealDish> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.in(SetmealDish::getDishId, ids);
        Long count = setmealDishMapper.selectCount(wrapper1);
        if (count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除菜品
        dishMapper.deleteBatchIds(ids);
        //删除与菜品关联的口味
        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(DishFlavor::getDishId, ids);
        dishFlavorMapper.delete(wrapper);
    }

    @Override
    public DishVO getDishVOById(Long id) {
        //查询dish
        Dish dish = dishMapper.selectById(id);
        //根据categoryId查询分类
        Category category = categoryMapper.selectById(dish.getCategoryId());
        //根据dishId查询口味
        List<DishFlavor> dishFlavors = dishFlavorMapper.selectList(new LambdaQueryWrapper<DishFlavor>().eq(DishFlavor::getDishId, id));
        //封装数据
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setCategoryName(category.getName());
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    @Override
    public void updateDish(DishDTO dishDTO) {
        //拆分数据为dish和flavors
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //更新菜品
        dishMapper.updateById(dish);
        //删除原有口味
        dishFlavorMapper.delete(new LambdaQueryWrapper<DishFlavor>().eq(DishFlavor::getDishId, dish.getId()));
        //提取口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        //判断是否有口味
        if (flavors != null && !flavors.isEmpty()) {
            //遍历给dishId赋值
            flavors.forEach(flavor -> flavor.setDishId(dish.getId()));
            //保存口味
            dishFlavorMapper.insertAll(flavors);
        }
    }

    @Override
    public List<Dish> getByCategoryId(Long categoryId) {
        //查询
        return dishMapper.selectList(new LambdaQueryWrapper<Dish>().eq(Dish::getCategoryId, categoryId));
    }

    @Override
    public List<DishVO> getDishVOListByCategoryId(Dish dishD) {
        //获得dishList集合
        List<Dish> dishList = dishMapper.selectList(
                new LambdaQueryWrapper<Dish>()
                        .eq(Dish::getCategoryId, dishD.getCategoryId())
                        .eq(Dish::getStatus, dishD.getStatus())
        );
        //创建dishVOList集合
        List<DishVO> dishVOList = new ArrayList<>();
        //为每一个dish封装flavorList
        dishList.forEach(dish -> {
            //根据dishId查询flavors
            List<DishFlavor> flavors = dishFlavorMapper.selectList(new LambdaQueryWrapper<DishFlavor>().eq(DishFlavor::getDishId, dish.getId()));
            //封装数据
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(dish, dishVO);
            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        });
        //返回dishVOList
        return dishVOList;
    }
}
