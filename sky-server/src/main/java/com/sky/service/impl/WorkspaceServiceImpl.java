package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.entity.Orders;
import com.sky.entity.Setmeal;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public BusinessDataVO getBusinessData() {
        //获取当日时间区间
        LocalDateTime begin = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        HashMap<Object, Object> map = new HashMap<>();
        map.put("begin", begin);
        map.put("end", end);

        //新增用户数
        Integer userCount = userMapper.getUserCount(map);
        userCount = userCount == null ? 0 : userCount;
        //获取当日订单
        Long orderCount = getOrderCount(map);
        orderCount = orderCount == null ? 0 : orderCount;
        //获取当日有效订单
        map.put("status", Orders.COMPLETED);
        Long validOrderCount = getOrderCount(map);
        validOrderCount = validOrderCount == null ? 0 : validOrderCount;
        //计算当日订单完成率
        Double orderRate = orderCount == 0 ? 0.0 : validOrderCount.doubleValue() / orderCount.doubleValue();
        //获取今日营业额
        Double amountSum = orderMapper.getAmountSum(map);
        amountSum = amountSum == null ? 0.0 : amountSum;
        //计算平均客单价
        Double averageAmount = validOrderCount == 0 ? 0.0 : amountSum / validOrderCount;

        return BusinessDataVO.builder()
                .newUsers(userCount)
                .orderCompletionRate(orderRate)
                .turnover(amountSum)
                .unitPrice(averageAmount)
                .validOrderCount(validOrderCount.intValue())
                .build();
    }

    @Override
    public OrderOverViewVO getOverviewOrders() {
        //获取当日时间区间
        LocalDateTime begin = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        HashMap<Object, Object> map = new HashMap<>();
        map.put("begin", begin);
        map.put("end", end);

        //全部订单
        Long allCount = getOrderCount(map);
        //已取消订单
        map.put("status", Orders.CANCELLED);
        Long cancelCount = getOrderCount(map);
        //已完成订单
        map.put("status", Orders.COMPLETED);
        Long completeCount = getOrderCount(map);
        //待派送订单
        map.put("status", Orders.CONFIRMED);
        Long deliveredCount = getOrderCount(map);
        //待接单订单
        map.put("status", Orders.TO_BE_CONFIRMED);
        Long waitingCount = getOrderCount(map);
        return OrderOverViewVO.builder()
                .allOrders(allCount.intValue())
                .cancelledOrders(cancelCount.intValue())
                .completedOrders(completeCount.intValue())
                .deliveredOrders(deliveredCount.intValue())
                .waitingOrders(waitingCount.intValue())
                .build();
    }

    @Override
    public DishOverViewVO getOverviewDishes() {
        //已停售菜品数量
        Long discontinued = getDishCount(StatusConstant.DISABLE);
        // 已启售菜品数量
        Long sold = getDishCount(StatusConstant.ENABLE);

        return DishOverViewVO.builder()
                .discontinued(discontinued.intValue())
                .sold(sold.intValue())
                .build();
    }

    @Override
    public SetmealOverViewVO getOverviewSetmeals() {
        //已停售套餐数量
        Long discontinued = getSetMealCount(StatusConstant.DISABLE);
        //已启售套餐数量
        Long sold = getSetMealCount(StatusConstant.ENABLE);

        return SetmealOverViewVO.builder()
                .discontinued(discontinued.intValue())
                .sold(sold.intValue())
                .build();
    }

    //获取订单数
    Long getOrderCount(Map<Object, Object> map) {
        Long count = orderMapper.selectCount(new LambdaQueryWrapper<Orders>()
                .ge(Orders::getOrderTime, map.get("begin"))
                .le(Orders::getOrderTime, map.get("end"))
                .eq(map.get("status") != null, Orders::getStatus, map.get("status")));
        return count == null ? 0L : count;
    }

    //查询菜品数量
    Long getDishCount(Integer status) {
        Long count = dishMapper.selectCount(new LambdaQueryWrapper<Dish>()
                .eq(status != null, Dish::getStatus, status));
        return count == null ? 0L : count;
    }

    //查询套餐数量
    Long getSetMealCount(Integer status) {
        Long count = setmealMapper.selectCount(new LambdaQueryWrapper<Setmeal>()
                .eq(status != null, Setmeal::getStatus, status));
        return count == null ? 0L : count;
    }
}
