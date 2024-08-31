package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
    /**
     * 获得单日营业额
     * @param map
     * @return
     */
    @Select("select sum(amount) from orders where order_time >= #{begin} and order_time <= #{end} and status = #{status};")
    Double getAmountSum(HashMap<Object, Object> map);

}
