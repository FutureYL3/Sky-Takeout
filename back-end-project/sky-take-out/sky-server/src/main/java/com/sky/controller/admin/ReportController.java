package com.sky.controller.admin;


import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@RestController
@Slf4j
@RequestMapping("/admin/report")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> turnoverReport(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end) {

        // 日志记录
        log.info("统计{}到{}的营业额", begin, end);
        // 调用service完成统计
        TurnoverReportVO data = reportService.turnoverReport(begin, end);

        return Result.success(data);
    }

    @GetMapping("/userStatistics")
    public Result<UserReportVO> userReport(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end) {

        // 日志记录
        log.info("统计{}到{}的用户数据", begin ,end);
        // 调用service完成统计
        UserReportVO data = reportService.userReport(begin, end);

        return Result.success(data);
    }

    @GetMapping("/ordersStatistics")
    public Result<OrderReportVO> orderReport(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end) {

        // 日志记录
        log.info("统计{}到{}的订单数据", begin, end);
        // 调用service完成统计
        OrderReportVO data = reportService.orderReport(begin, end);

        return Result.success(data);
    }

    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> salesTop10(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end) {

        // 日志记录
        log.info("查询{}到{}的销量前十", begin, end);
        // 调用service完成查询
        SalesTop10ReportVO data = reportService.salesTop10(begin, end);

        return Result.success(data);
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        // 日志记录
        log.info("将近30天的运营数据导出为Excel报表");
        // 调用service完成报表导出
        reportService.export(response);

    }
}
