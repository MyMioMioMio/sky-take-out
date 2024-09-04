package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.webSocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import com.sky.utils.WeChatPayUtil;

@Service
public class OrderServiceImpl implements OrderService {


    @Autowired
    ShoppingCartMapper shoppingCartMapper;

    @Autowired
    AddressBookMapper addressBookMapper;

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    WebSocketServer webSocketServer;


    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        // 判断有无地址
        AddressBook addressBook = addressBookMapper.selectById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        // 判断购物车是否为空
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectList(new LambdaQueryWrapper<ShoppingCart>().eq(ShoppingCart::getUserId, BaseContext.getCurrentId()));
        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        // 插入一条订单信息
        // 封装信息
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setNumber(UUID.randomUUID().toString() + "-" + BaseContext.getCurrentId());
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setUserId(BaseContext.getCurrentId());
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setAddress(addressBook.getFullAddress());
        orders.setPackAmount(ordersSubmitDTO.getPackAmount());
        orderMapper.insert(orders);
        // 插入多条订单详情信息
        // 封装订单详情
        ArrayList<OrderDetail> orderDetails = new ArrayList<>();
        shoppingCartList.forEach(shoppingCart -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetails.add(orderDetail);
        });
        orderDetailMapper.insertBatch(orderDetails);
        // 清空购物车
        shoppingCartMapper.delete(new LambdaQueryWrapper<ShoppingCart>().eq(ShoppingCart::getUserId, BaseContext.getCurrentId()));

        //封装VO
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .build();
        return orderSubmitVO;
    }


    /**
     * 订单支付
     * 已修改为虚假支付
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
//        User user = userMapper.getById(userId);
        User user = userMapper.selectById(userId);

        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
        //修改后的，直接跳过微信支付接口
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "ORDERPAID");
        paySuccess(ordersPaymentDTO.getOrderNumber());
        //----------------------------------------------------

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
//        Orders ordersDB = orderMapper.getByNumber(outTradeNo);
        Orders ordersDB = orderMapper.selectOne(new LambdaQueryWrapper<Orders>().eq(Orders::getNumber, outTradeNo));

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .packAmount(ordersDB.getPackAmount())
                .build();

//        orderMapper.update(orders);
        orderMapper.updateById(orders);

        //通过websocket实现来单提醒
        Map<String, Object> map = new HashMap<>();
        map.put("type", Orders.ORDERS_INCOMING);
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号：" + ordersDB.getNumber());
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    @Override
    public PageResult getHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        //分页查询本用户的所有订单
        IPage<Orders> page = new Page<>(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        orderMapper.selectPage(
                page,
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getUserId, BaseContext.getCurrentId())
                        .eq(ordersPageQueryDTO.getStatus() != null, Orders::getStatus, ordersPageQueryDTO.getStatus())
                        .orderByDesc(Orders::getOrderTime));
        ArrayList<OrderVO> orderVOS = new ArrayList<>();
        page.getRecords().forEach(orders -> {
            OrderVO orderVO = new OrderVO();
            //查询订单详情
            List<OrderDetail> orderDetails = orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, orders.getId()));
            //封装vo
            BeanUtils.copyProperties(orders, orderVO);
            orderVO.setOrderDetailList(orderDetails);
            orderVOS.add(orderVO);
        });
        return new PageResult(page.getTotal(), orderVOS);
    }

    @Override
    public OrderVO getOrderDetail(Long id) {
        //查询订单
        Orders orders = orderMapper.selectById(id);
        //查询订单地址
        AddressBook addressBook = addressBookMapper.selectById(orders.getAddressBookId());
        //查询订单详情
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, orders.getId()));
        //封装vo
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetails);
        orderVO.setAddress(addressBook.getFullAddress());
        return orderVO;
    }

    @Override
    public void cancelOrder(Long id) {
        //查询订单
        Orders orderDB = orderMapper.selectById(id);
        //判断订单是否存在
        if (orderDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //判断订单状态 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        //1待付款 2待接单才能用户自行取消
        if (orderDB.getStatus().intValue() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        orders.setId(id);
        //订单已付款则退款
        if (orderDB.getPayStatus().intValue() == Orders.PAID) {
            //调用退款方法
            /*****/
            orders.setPayStatus(Orders.REFUND);
        }
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消订单");
        orders.setCancelTime(LocalDateTime.now());
        //更新订单状态为已取消
        orderMapper.updateById(orders);
    }

    @Override
    public void repetition(Long id) {
        //清空当前用户购物车
        shoppingCartMapper.delete(new LambdaQueryWrapper<ShoppingCart>().eq(ShoppingCart::getUserId, BaseContext.getCurrentId()));
        //根据订单id查询订单商品
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, id));
        //依次添加商品进入购物车
        orderDetails.forEach(orderDetail -> {
            ShoppingCart shoppingCart = ShoppingCart.builder()
                    .name(orderDetail.getName())
                    .image(orderDetail.getImage())
                    .userId(BaseContext.getCurrentId())
                    .dishId(orderDetail.getDishId())
                    .setmealId(orderDetail.getSetmealId())
                    .dishFlavor(orderDetail.getDishFlavor())
                    .number(orderDetail.getNumber())
                    .amount(orderDetail.getAmount()).build();
            shoppingCartMapper.insert(shoppingCart);
        });
    }

    @Override
    public void reminder(Long id) {
        //查询订单
        Orders orders = orderMapper.selectById(id);
        //判断订单是否存在或者是否为已完成状态
        if (orders == null || orders.getStatus() == Orders.COMPLETED) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //发送催单信息
        Map<String, String> map = new HashMap<>();
        map.put("type", Orders.ORDERS_REMINDER);
        map.put("orderId", id.toString());
        map.put("content", "订单号：" + orders.getNumber());
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    @Override
    public PageResult getPageOrder(OrdersPageQueryDTO ordersPageQueryDTO) {
        IPage<Orders> page = new Page<>(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        orderMapper.selectPage(page,
                new LambdaQueryWrapper<Orders>()
                        .like(ordersPageQueryDTO.getNumber() != null, Orders::getNumber, ordersPageQueryDTO.getNumber())
                        .like(ordersPageQueryDTO.getPhone() != null, Orders::getPhone, ordersPageQueryDTO.getPhone())
                        .eq(ordersPageQueryDTO.getStatus() != null, Orders::getStatus, ordersPageQueryDTO.getStatus())
                        .ge(ordersPageQueryDTO.getBeginTime() != null, Orders::getOrderTime, ordersPageQueryDTO.getBeginTime())
                        .le(ordersPageQueryDTO.getEndTime() != null, Orders::getOrderTime, ordersPageQueryDTO.getEndTime())
                        .orderByDesc(Orders::getOrderTime));
        //需要将Orders转换为OrderVo
        List<OrderVO> orderVOList = getOrderVOList(page.getRecords());
        return new PageResult(page.getTotal(), orderVOList);
    }

    /**
     * 将Orders转换为OrderVO
     * @param ordersList
     * @return
     */
    private List<OrderVO> getOrderVOList(List<Orders> ordersList) {
        List<OrderVO> orderVOList = new ArrayList<>();
        ordersList.forEach(orders -> {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);
            //设置订单详细字符串
            orderVO.setOrderDishes(getOrderDishes(orders));
            orderVOList.add(orderVO);
        });
        return orderVOList;
    }

    /**
     * 获取订单详细字符串
     * @param orders
     * @return
     */
    private String getOrderDishes(Orders orders) {
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, orders.getId()));
        List<String> orderDishesStr = new ArrayList<>();
        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        orderDetails.forEach(orderDetail -> {
            String dishName = orderDetail.getName() + "*" + orderDetail.getNumber() + "；";
            orderDishesStr.add(dishName);
        });
        return String.join("", orderDishesStr);
    }

    @Override
    public OrderStatisticsVO getStatistics() {
        //待派送数量
        Long confirmC = orderMapper.selectCount(new LambdaQueryWrapper<Orders>().eq(Orders::getStatus, Orders.CONFIRMED));
        //派送中数量
        Long deliveryC = orderMapper.selectCount(new LambdaQueryWrapper<Orders>().eq(Orders::getStatus, Orders.DELIVERY_IN_PROGRESS));
        //待接单数量
        Long toConfirmC = orderMapper.selectCount(new LambdaQueryWrapper<Orders>().eq(Orders::getStatus, Orders.TO_BE_CONFIRMED));
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(confirmC.intValue());
        orderStatisticsVO.setToBeConfirmed(toConfirmC.intValue());
        orderStatisticsVO.setDeliveryInProgress(deliveryC.intValue());
        return orderStatisticsVO;
    }

    @Override
    public void confirm(OrdersDTO ordersDTO) {
        //查询订单
        Orders orders = orderMapper.selectById(ordersDTO.getId());
        //判断订单是否存在
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //判断订单是否为待接单
        if (orders.getStatus().intValue() != Orders.TO_BE_CONFIRMED) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //修改订单状态
        orders.setStatus(Orders.CONFIRMED);
        orderMapper.updateById(orders);
    }

    @Override
    public void delivery(Long id) {
        //查询订单
        Orders orders = orderMapper.selectById(id);
        //判断订单是否存在
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //判断订单是否为已接单
        if (orders.getStatus().intValue() != Orders.CONFIRMED) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //修改订单状态
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.updateById(orders);
    }

    @Override
    public void complete(Long id) {
        //查询订单
        Orders orders = orderMapper.selectById(id);
        //判断订单是否存在
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //判断订单是否为派送中
        if (orders.getStatus().intValue() != Orders.DELIVERY_IN_PROGRESS) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //修改订单状态
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.updateById(orders);
    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        //查询订单
        Orders orders = orderMapper.selectById(ordersCancelDTO.getId());
        //判断订单是否存在
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //若付款则先退款
        if (orders.getPayStatus().intValue() == Orders.PAID) {
            //调用退款方法
            /***********/
            //修改支付状态
            orders.setPayStatus(Orders.REFUND);
        }
        //修改订单状态
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.updateById(orders);
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        //查询订单
        Orders orders = orderMapper.selectById(ordersRejectionDTO.getId());
        //判断订单是否存在
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //若付款则先退款
        if (orders.getPayStatus().intValue() == Orders.PAID) {
            //调用退款方法
            /***********/
            //修改支付状态
            orders.setPayStatus(Orders.REFUND);
        }
        //修改订单状态
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.updateById(orders);
    }
}
