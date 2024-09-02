package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
    /**
     * 批量插入
     * @param orderDetails
     */
    void insertBatch(ArrayList<OrderDetail> orderDetails);


    /**
     * 获取指定订单范围内的商品前10销量
     * @param orderIdList
     * @return
     */
    @MapKey("name")
    List<Map<String, Object>> getSales(@Param("orderIdList") List<Long> orderIdList);
}
