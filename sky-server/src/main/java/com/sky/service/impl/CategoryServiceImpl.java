package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetMealMapper setMealMapper;

    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        //封装页数
        IPage<Category> page = new Page<>(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
        //封装查询条件
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        String name = categoryPageQueryDTO.getName();
        Integer type = categoryPageQueryDTO.getType();
        wrapper.like(name != null && !name.isEmpty(), Category::getName, name)
                .eq(type != null, Category::getType, type)
                .orderByAsc(Category::getSort)
                .orderByDesc(Category::getCreateTime);
        //分页查询
        categoryMapper.selectPage(page, wrapper);
        return new PageResult(page.getTotal(), page.getRecords());
    }

    @Override
    public void changStatus(Integer status, Long id) {
        //封装参数
        Category category = Category.builder()
                .id(id)
                .status(status)
                .build();
        //启用、禁用分类
        categoryMapper.updateById(category);
    }

    @Override
    public void save(CategoryDTO categoryDTO) {
        //封装数据
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
//        //设置创建人，创建日期，最后修改日期，最后修改人
//        category.setCreateUser(BaseContext.getCurrentId());
//        category.setCreateTime(LocalDateTime.now());
//        category.setUpdateTime(LocalDateTime.now());
//        category.setUpdateUser(BaseContext.getCurrentId());
        //保存
        categoryMapper.insert(category);
    }

    @Override
    public void update(CategoryDTO categoryDTO) {
        //封装数据
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
//        //设置修改日期和最后修改人
//        category.setUpdateUser(BaseContext.getCurrentId());
//        category.setUpdateTime(LocalDateTime.now());
        //修改
        categoryMapper.updateById(category);
    }

    @Override
    public void delete(Long id) {
        //判断分类是否关联菜品
        Long dc = dishMapper.selectCount(new LambdaQueryWrapper<Dish>().eq(Dish::getCategoryId, id));
        if (dc > 0) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }
        //判断分类是否关联套餐
        Long sc = setMealMapper.selectCount(new LambdaQueryWrapper<Setmeal>().eq(Setmeal::getCategoryId, id));
        if (sc > 0) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }
        //删除分类
        categoryMapper.deleteById(id);
    }

    @Override
    public List<Category> selectList(Integer type) {
        //封装条件
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getType, type);
        //查询
        return categoryMapper.selectList(wrapper);
    }
}
