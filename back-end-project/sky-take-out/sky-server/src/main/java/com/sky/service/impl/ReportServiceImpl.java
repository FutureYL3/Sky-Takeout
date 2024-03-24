package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.mapper.ReportMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ReportServiceImpl implements ReportService {
    private final ReportMapper reportMapper;
    private final WorkspaceService workspaceService;
    @Override
    public TurnoverReportVO turnoverReport(LocalDate begin, LocalDate end) {
        // 封装返回的list集合
        ArrayList<LocalDate> times = new ArrayList<>();
        times.add(begin);
        int count = 1;
        while (true) {
            LocalDate added = begin.plusDays(count++);
            if (added.isAfter(end)) {
                break;
            }
            times.add(added);
        }
        // 查询在begin和end之间每天的营业额
        ArrayList<Double> turnovers = new ArrayList<>();
        for (LocalDate time : times) {
            LocalDateTime beginTime = LocalDateTime.of(time, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(time, LocalTime.MAX);
            Double turnover = reportMapper.getTurnover(beginTime, endTime);
            turnover = turnover == null ? 0.0 : turnover;
            turnovers.add(turnover);
        }

        // 封装返回数据
        return new TurnoverReportVO(StringUtils.join(times, ","), StringUtils.join(turnovers, ","));

    }

    @Override
    public UserReportVO userReport(LocalDate begin, LocalDate end) {
        // 封装返回的list集合
        ArrayList<LocalDate> times = new ArrayList<>();
        times.add(begin);
        int count = 1;
        while (true) {
            LocalDate added = begin.plusDays(count++);
            if (added.isAfter(end)) {
                break;
            }
            times.add(added);
        }
        // 拿到每日用户总量和新增用户数量
        ArrayList<Integer> newUserNums = new ArrayList<>();
        ArrayList<Integer> totalUserNums = new ArrayList<>();
        for (LocalDate time : times) {
            LocalDateTime beginTime = LocalDateTime.of(time, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(time, LocalTime.MAX);
            Integer newUserNum = reportMapper.newUserNum(beginTime, endTime);
            Integer totalUserNum = reportMapper.totalUserNum(endTime);

            newUserNums.add(newUserNum);
            totalUserNums.add(totalUserNum);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(times, ","))
                .newUserList(StringUtils.join(newUserNums, ","))
                .totalUserList(StringUtils.join(totalUserNums, ","))
                .build();

    }

    @Override
    public OrderReportVO orderReport(LocalDate begin, LocalDate end) {
        // 封装返回的list集合
        ArrayList<LocalDate> times = new ArrayList<>();
        times.add(begin);
        int count = 1;
        while (true) {
            LocalDate added = begin.plusDays(count++);
            if (added.isAfter(end)) {
                break;
            }
            times.add(added);
        }
        // 查询需要的订单数据
        ArrayList<Integer> totalOrderNums = new ArrayList<>();
        ArrayList<Integer> validOrderNums = new ArrayList<>();
        Integer totalNums;
        Integer validNums;
        for (LocalDate time : times) {
            LocalDateTime beginTime = LocalDateTime.of(time, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(time, LocalTime.MAX);
            Integer totalOrderNum = reportMapper.totalOrderNum(beginTime, endTime);
            Integer validOrderNum = reportMapper.validOrderNum(beginTime, endTime);
            totalOrderNums.add(totalOrderNum);
            validOrderNums.add(validOrderNum);
        }
        totalNums = totalOrderNums.stream().mapToInt(Integer::intValue).sum();
        validNums = validOrderNums.stream().mapToInt(Integer::intValue).sum();
        Double completionRate = totalNums == 0 ? 0.0 : 1.0 * validNums / totalNums;
        return OrderReportVO.builder()
                .dateList(StringUtils.join(times, ","))
                .orderCountList(StringUtils.join(totalOrderNums, ","))
                .validOrderCountList(StringUtils.join(validOrderNums, ","))
                .totalOrderCount(totalNums)
                .validOrderCount(validNums)
                .orderCompletionRate(completionRate)
                .build();

    }

    @Override
    public SalesTop10ReportVO salesTop10(LocalDate begin, LocalDate end) {
        // 拿到该时间段内的销量前十的菜品
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> data = reportMapper.salesTop10(beginTime, endTime);
        List<String> names = data.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numbers = data.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(names, ","))
                .numberList(StringUtils.join(numbers, ","))
                .build();
    }

    @Override
    public void export(HttpServletResponse response) {
        InputStream in;
        ServletOutputStream out;
        XSSFWorkbook excel;
        // 1. 查询数据库，获得营业数据
        // 拿到概览数据
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(begin, LocalTime.MIN),
                LocalDateTime.of(end, LocalTime.MAX));
        try {
            // 2. 将营业数据通过POI写入到Excel样板中
            in = this.getClass().getClassLoader().getResourceAsStream("template/Excel-Template.xlsx");
            // 基于模版文件创建一个workbook
            excel = new XSSFWorkbook(in);
            //获取表格文件的Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + begin + "至" + end);

            //获得第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = begin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            // 3. 通过输出流将文件输出到客户端浏览器
            out = response.getOutputStream();
            excel.write(out);

            // 4. 关闭资源
            out.close();
            excel.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
