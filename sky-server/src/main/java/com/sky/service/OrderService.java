package com.sky.service;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.vo.OrderSubmitVO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface OrderService {
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);
}
