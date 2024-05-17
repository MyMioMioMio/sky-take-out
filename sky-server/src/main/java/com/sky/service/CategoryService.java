package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface CategoryService {

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 启用、禁用分类
     *
     * @param status
     * @param id
     */
    void changStatus(Integer status, Long id);

    /**
     * 新增分类
     * @param categoryDTO
     */
    void save(CategoryDTO categoryDTO);

    /**
     * 修改分类
     * @param categoryDTO
     */
    void update(CategoryDTO categoryDTO);

    /**
     * 根据id删除分类
     * @param id
     */
    void delete(Long id);

    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    List<Category> selectList(Integer type);
}
