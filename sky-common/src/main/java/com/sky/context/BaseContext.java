package com.sky.context;

/**
 * threadlocal的工具类
 */
public class BaseContext {

    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 存储当前用户ID
     * @param id
     */
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    /**
     * 获取当前用户ID
     * @return
     */
    public static Long getCurrentId() {
        return threadLocal.get();
    }

    /**
     * 移除当前用户ID
     */
    public static void removeCurrentId() {
        threadLocal.remove();
    }

}
