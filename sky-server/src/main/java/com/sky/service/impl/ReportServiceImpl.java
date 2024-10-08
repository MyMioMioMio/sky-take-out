package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private WorkspaceService workspaceService;


    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //日期列表
        List<LocalDate> dateList = getLocalDateList(begin, end);
        //营业额列表
        List<Double> turnoverList = new ArrayList<>();
        dateList.forEach(date -> {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            HashMap<Object, Object> map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double amount = orderMapper.getAmountSum(map);
            if (amount == null) amount = 0.0;
            turnoverList.add(amount);
        });
        return new TurnoverReportVO(StringUtils.join(dateList, ","), StringUtils.join(turnoverList, ","));
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //日期列表
        List<LocalDate> dateList = getLocalDateList(begin, end);
        //总用户列表
        List<Integer> userList = new ArrayList<>();
        //新增用户列表
        List<Integer> newUserList = new ArrayList<>();
        dateList.forEach(date -> {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //查找本日总用户
            Integer total = getUserCount(null, endTime);
            userList.add(total);
            //查找本日新增用户
            Integer newUser = getUserCount(beginTime, endTime);
            newUserList.add(newUser);
        });
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(userList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
//        LocalDateTime beginTimeF = LocalDateTime.of(begin, LocalTime.MIN);
//        LocalDateTime endTimeF = LocalDateTime.of(end, LocalTime.MAX);
//        //获取订单总数
//        Long totalOrder = orderMapper.selectCount(new LambdaQueryWrapper<Orders>().ge(Orders::getOrderTime, beginTimeF).le(Orders::getOrderTime, endTimeF));
//        //获取已完成订单数
//        Long validOrder = orderMapper.selectCount(new LambdaQueryWrapper<Orders>().ge(Orders::getOrderTime, beginTimeF).le(Orders::getOrderTime, endTimeF).eq(Orders::getStatus, Orders.COMPLETED));

        //时间区间内订单总数
        AtomicInteger totalOrder = new AtomicInteger();
        //时间区间内已完成订单数
        AtomicInteger validOrder = new AtomicInteger();
        //获取日期列表
        List<LocalDate> dateList = getLocalDateList(begin, end);
        //每日订单数列表和每日订单完成数列表
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        dateList.forEach(date -> {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //每日订单数
            Long orderCount = orderMapper.selectCount(new LambdaQueryWrapper<Orders>().ge(Orders::getOrderTime, beginTime).le(Orders::getOrderTime, endTime));
            orderCountList.add(orderCount.intValue());
            totalOrder.addAndGet(orderCount.intValue());
            //每日订单完成数
            Long validOrderCount = orderMapper.selectCount(new LambdaQueryWrapper<Orders>().ge(Orders::getOrderTime, beginTime).le(Orders::getOrderTime, endTime).eq(Orders::getStatus, Orders.COMPLETED));
            validOrderCountList.add(validOrderCount.intValue());
            validOrder.addAndGet(validOrderCount.intValue());
        });

        //订单完成率
        Double orderCompletionRate = totalOrder.intValue() == 0 ? 0.0 : validOrder.doubleValue() / totalOrder.doubleValue();

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCompletionRate(orderCompletionRate)
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrder.intValue())
                .validOrderCount(validOrder.intValue())
                .build();
    }

    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        //获取完整日期
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        //获取日期区间订单的orderId
        List<Long> orderIdList = orderMapper.selectList(new LambdaQueryWrapper<Orders>().ge(Orders::getOrderTime, beginTime).le(Orders::getOrderTime, endTime).eq(Orders::getStatus, Orders.COMPLETED))
                .stream()
                .map(Orders::getId)
                .collect(Collectors.toList());
        //top10的商品名称列表和销量列表
        List<String> nameList = new ArrayList<>();
        List<Object> sumList = new ArrayList<>();
        //若orderIdList非空则获取top10的商品销量
        if (!orderIdList.isEmpty()) {
            List<GoodsSalesDTO> top10List =  orderDetailMapper.getSales(orderIdList);
            nameList = top10List.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
            sumList = top10List.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        }
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(sumList, ","))
                .build();
    }

    @Override
    public void exportExcel(HttpServletResponse response) {
        //查询信息（最近30天信息）
        //设置日期
        LocalDate beginDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now().minusDays(1);
        //获取概览数据
        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(beginDate, LocalTime.MIN), LocalDateTime.of(endDate, LocalTime.MAX));

        //通过POI导出Excel
        //获取excel对象
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(in);
            XSSFSheet sheet = workbook.getSheet("Sheet1");
            //填充日期信息
            XSSFRow row = sheet.getRow(1);
            row.getCell(1).setCellValue("日期：" + beginDate.toString() + "至" + endDate.toString());
            //填充概览信息
            row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());
            //填充明细信息
            for (int i = 0; i < 30; i++) {
                BusinessDataVO businessDataToDay = workspaceService.getBusinessData(LocalDateTime.of(endDate, LocalTime.MIN), LocalDateTime.of(endDate, LocalTime.MAX));
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(endDate.toString());
                row.getCell(2).setCellValue(businessDataToDay.getTurnover());
                row.getCell(3).setCellValue(businessDataToDay.getValidOrderCount());
                row.getCell(4).setCellValue(businessDataToDay.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessDataToDay.getUnitPrice());
                row.getCell(6).setCellValue(businessDataToDay.getNewUsers());
                endDate = endDate.minusDays(1);
            }

            //通过输出流输出到客户端浏览器
            ServletOutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            //释放资源
            outputStream.close();
            in.close();
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 返回日期begin到end之间的总用户数
     * @param begin
     * @param end
     * @return
     */
    private Integer getUserCount(LocalDateTime begin, LocalDateTime end) {
        HashMap<Object, Object> map = new HashMap<>();
        map.put("begin", begin);
        map.put("end", end);
        Integer count = userMapper.getUserCount(map);
        if (count == null) count = 0;
        return count;
    }

    /**
     * 返回列表包含begin到end之间的每一天日期
     * @param begin
     * @param end
     * @return
     */
    private List<LocalDate> getLocalDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        while (!begin.equals(end.plusDays(1))) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        return dateList;
    }
}
