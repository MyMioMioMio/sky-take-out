package com.sky.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时处理订单类任务
 */
@Component
@Slf4j
public class OrderTask {
    @Autowired
    OrderMapper orderMapper;

    /**
     * 每分钟自动处理付款超时订单
     */
    @Scheduled(cron = "0 * * * * ?")
    public void proceedTimeoutOrder() {
        log.info("定时任务处理付款超时订单:{}", LocalDateTime.now());
        LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(-15);
        List<Orders> orders = orderMapper.selectList(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getStatus, Orders.PENDING_PAYMENT)
                .lt(Orders::getOrderTime, localDateTime));
        if (orders != null && !orders.isEmpty()) {
            orders.forEach(order -> {
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason(Orders.PAYMENT_TIMEOUT);
                order.setCancelTime(LocalDateTime.now());
                orderMapper.updateById(order);
            });
        }
    }

    /**
     * 定时处理派送完成的订单
     * 每日凌晨一点
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void proceedDeliveryOrder() {
        log.info("定时处理派送完成的订单:{}", LocalDateTime.now());
        LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(-60);
        List<Orders> orders = orderMapper.selectList(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getStatus, Orders.DELIVERY_IN_PROGRESS)
                .lt(Orders::getOrderTime, localDateTime));
        if (orders != null && !orders.isEmpty()) {
            orders.forEach(order -> {
                order.setStatus(Orders.COMPLETED);
                orderMapper.updateById(order);
            });
        }
    }
}
