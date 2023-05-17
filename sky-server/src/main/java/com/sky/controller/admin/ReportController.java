package com.sky.controller.admin;

import com.sky.entity.User;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/**
 * @ClassName ReportController
 * @Description TODO
 * @Author XMING
 * @Date 2023/5/17 9:39
 * @Version 1.0
 */
@RestController
@Slf4j
@RequestMapping("/admin/report")
@Api(tags = "数据统计接口")
public class ReportController {
    @Autowired
    private ReportService reportService;

    /**
     * 查询begin-end日期内的营业额数据
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额数据统计")
    public Result<TurnoverReportVO> turnoverStats(
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end
            ) {
            TurnoverReportVO stats = reportService.turnoverStats(begin, end);
            return Result.success(stats);
    }

    /**
     * 用户数据统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/userStatistics")
    @ApiOperation("用户统计数据")
    public Result<UserReportVO> userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end
    ){
        UserReportVO userReport = reportService.userStats(begin,end);
        return Result.success(userReport);
    }

    @GetMapping("/ordersStatistics")
    @ApiOperation("订单数据统计")
    public Result<OrderReportVO> orderStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end
    ){
        return Result.success(reportService.orderStatistics(begin,end));
    }

    /**
     * 统计时间区间内销量前10的商品
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/top10")
    @ApiOperation("销量前10商品数据统计")
    public Result<SalesTop10ReportVO> top10(
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end
    ){
        SalesTop10ReportVO salesTop10ReportVO = reportService.top10(begin,end);
        return Result.success(salesTop10ReportVO);
    }

    /**
     * 到处运营报表
     * @param response
     */
    @GetMapping("/export")
    @ApiOperation("导出运营报表")
    public void export(HttpServletResponse response){
        reportService.exportBuisness(response);
    }
}
