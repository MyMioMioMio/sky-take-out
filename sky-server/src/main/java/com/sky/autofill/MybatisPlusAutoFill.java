package com.sky.autofill;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 实现mybatis-plus的公共自动填充
 */
@Slf4j
@Component
public class MybatisPlusAutoFill implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("公共字段自动填充:{}", metaObject);
        metaObject.setValue(AutoFillConstant.CREATE_TIME, LocalDateTime.now());
        metaObject.setValue(AutoFillConstant.CREATE_USER, BaseContext.getCurrentId());

        metaObject.setValue(AutoFillConstant.UPDATE_TIME, LocalDateTime.now());
        metaObject.setValue(AutoFillConstant.UPDATE_USER, BaseContext.getCurrentId());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共字段自动填充:{}", metaObject);
        metaObject.setValue(AutoFillConstant.UPDATE_TIME, LocalDateTime.now());
        metaObject.setValue(AutoFillConstant.UPDATE_USER, BaseContext.getCurrentId());
    }
}
