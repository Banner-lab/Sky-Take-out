package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @ClassName ReportServiceImpl
 * @Description TODO
 * @Author XMING
 * @Date 2023/5/17 9:51
 * @Version 1.0
 */
@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 统计指定时间区间内每天营业额情况
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO turnoverStats(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = dateList(begin,end);

        String dateL = StringUtils.join(dateList, ",");
        log.info("时间区间:{}",dateL);

        // 查询订单表，计算时间区间内每天的营业额
        List<Double> amountList = new ArrayList<>();
        dateList.forEach(d->{
            LocalDateTime start = d.atStartOfDay();
            LocalDateTime last = d.atTime(23,59,59);

            Double amount = orderMapper.queryByDate(start,last,Orders.COMPLETED);
            amount = amount == null ? 0.0 : amount;
            amountList.add(amount);
        });

        log.info("每天的营业额为:{}",amountList);

        return TurnoverReportVO.builder().dateList(dateL).turnoverList(StringUtils.join(amountList,",")).build();
    }

    /**
     * 用户数据统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO userStats(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = dateList(begin,end);
        // 查询每天新增用户数量
        List<Integer> newUsers = new ArrayList<>();
        // 存放每天的总用户量
        List<Integer> totalUsers = new ArrayList<>();
        dateList.forEach(d->{
            LocalDateTime start = d.atStartOfDay();
            LocalDateTime last = d.atTime(23,59,59);
            Integer cnt = userMapper.countUsers(start,last);

            Integer cnt1 = userMapper.countUsers(null,last);
            cnt1 = cnt1 == null ? 0 : cnt1;

            cnt = cnt == null ? 0 : cnt;
            newUsers.add(cnt);
            totalUsers.add(cnt1);
        });


        return UserReportVO.builder().dateList(StringUtils.join(dateList,","))
                .newUserList(StringUtils.join(newUsers,","))
                .totalUserList(StringUtils.join(totalUsers,","))
                .build();
    }

    /**
     * 订单数据统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO orderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = dateList(begin, end);
        // 查询每日订单数,有效订单数
        Integer totalOrders = 0;
        Integer completedOrders = 0;

        List<Integer> total = new ArrayList<Integer>();
        List<Integer> completed = new ArrayList<Integer>();

        for(LocalDate d : dateList){
            LocalDateTime start = d.atStartOfDay();
            LocalDateTime last = d.atTime(23,59,59);

            // 每日总订单数
            Integer countEveryDay = orderMapper.countOrders(start,last,null);
            Integer countCompletedEveryDay = orderMapper.countOrders(start,last,Orders.COMPLETED);

            countEveryDay = countEveryDay == null ? 0 : countEveryDay;
            countCompletedEveryDay = countCompletedEveryDay == null ? 0 : countCompletedEveryDay;
            total.add(countEveryDay);
            completed.add(countCompletedEveryDay);

            totalOrders += countEveryDay;
            completedOrders += countCompletedEveryDay;
        }
        return OrderReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(total,","))
                .validOrderCountList(StringUtils.join(completed,","))
                .totalOrderCount(totalOrders)
                .validOrderCount(completedOrders)
                .orderCompletionRate((double) completedOrders / totalOrders)
                .build();
    }

    /**
     * 销量前10商品数据统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        LocalDateTime start = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime last = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> goodsSalesDTOS = orderMapper.queryGoodsSalesTop10(start, last);

        List<String> names = goodsSalesDTOS.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numbers = goodsSalesDTOS.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        return SalesTop10ReportVO
                .builder()
                .nameList(StringUtils.join(names, ","))
                .numberList(StringUtils.join(numbers, ","))
                .build();
    }

    /**
     * 生成运营数据报表
     * @param response
     */
    @Override
    public void exportBuisness(HttpServletResponse response) {
        // 查询过去30天的运营数据
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(begin,LocalTime.MIN), LocalDateTime.of(end,LocalTime.MAX));

        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
            XSSFWorkbook excel = new XSSFWorkbook(is);
            XSSFSheet sheet1 = excel.getSheet("Sheet1");
            sheet1.getRow(1).getCell(1).setCellValue("时间:"+begin+"至"+end);

            sheet1.getRow(3).getCell(2).setCellValue(businessData.getTurnover());
            sheet1.getRow(3).getCell(4).setCellValue(businessData.getOrderCompletionRate());
            sheet1.getRow(3).getCell(6).setCellValue(businessData.getNewUsers());

            sheet1.getRow(4).getCell(2).setCellValue(businessData.getValidOrderCount());
            sheet1.getRow(4).getCell(4).setCellValue(businessData.getUnitPrice());

            // 生成每日运营数据
            for(int i =0;i<30;i++){
                LocalDate start = begin.plusDays(i);
                BusinessDataVO bd = workspaceService.getBusinessData(LocalDateTime.of(start, LocalTime.MIN), LocalDateTime.of(start, LocalTime.MAX));
                sheet1.getRow(i+7).getCell(1).setCellValue(String.valueOf(start));
                sheet1.getRow(i+7).getCell(2).setCellValue(bd.getTurnover());
                sheet1.getRow(i+7).getCell(3).setCellValue(bd.getValidOrderCount());
                sheet1.getRow(i+7).getCell(4).setCellValue(bd.getOrderCompletionRate());
                sheet1.getRow(i+7).getCell(5).setCellValue(bd.getUnitPrice());
                sheet1.getRow(i+7).getCell(6).setCellValue(bd.getNewUsers());

            }


            // 获取输出流，供用户下载
            ServletOutputStream oos = response.getOutputStream();

            excel.write(oos);

            oos.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<LocalDate> dateList(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        return dateList;
    }
}
