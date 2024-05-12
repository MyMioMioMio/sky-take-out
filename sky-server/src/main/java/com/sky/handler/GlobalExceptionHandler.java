package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * (方法的重载)
     * 捕获SQL异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        //获得异常信息 Duplicate entry 'zhangsan' for key 'employee.idx_username'
        String message = ex.getMessage();
        //判断
        if (message.contains("Duplicate entry")) {
            //属性重复
            //获得属性内容
            String[] split = message.split(" ");
            String property = split[2];
            return Result.error(property + MessageConstant.ALREADY_EXIST);
        } else {
            //未知异常
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }
}
