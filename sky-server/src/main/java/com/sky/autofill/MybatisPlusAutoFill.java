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
        if (metaObject.hasSetter(AutoFillConstant.CREATE_TIME)) {
            metaObject.setValue(AutoFillConstant.CREATE_TIME, LocalDateTime.now());
        }
        if (metaObject.hasSetter(AutoFillConstant.CREATE_USER)) {
            metaObject.setValue(AutoFillConstant.CREATE_USER, BaseContext.getCurrentId());
        }

        if (metaObject.hasSetter(AutoFillConstant.UPDATE_TIME)) {
            metaObject.setValue(AutoFillConstant.UPDATE_TIME, LocalDateTime.now());
        }
        if (metaObject.hasSetter(AutoFillConstant.UPDATE_USER)) {
            metaObject.setValue(AutoFillConstant.UPDATE_USER, BaseContext.getCurrentId());
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共字段自动填充:{}", metaObject);
        if (metaObject.hasSetter(AutoFillConstant.UPDATE_TIME)) {
            metaObject.setValue(AutoFillConstant.UPDATE_TIME, LocalDateTime.now());
        }
        if (metaObject.hasSetter(AutoFillConstant.UPDATE_USER)) {
            metaObject.setValue(AutoFillConstant.UPDATE_USER, BaseContext.getCurrentId());
        }
    }
}
