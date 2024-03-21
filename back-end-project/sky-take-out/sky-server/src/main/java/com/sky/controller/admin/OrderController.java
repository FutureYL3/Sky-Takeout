package com.sky.controller.admin;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: OrderController
 * <p>
 * Package: com.sky.controller.admin
 * <p>
 * Description:
 * <p>
 *
 * @Author: yl
 * @Create: 2024/3/21 - 11:56
 * @Version: v1.0
 */
@RestController
@Slf4j
@RequestMapping("/admin/order")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/conditionSearch")
    public Result<PageResult> pageOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 日志记录
        log.info("管理端搜索订单：{}", ordersPageQueryDTO);
        // 调用service完成搜索
        PageResult data = orderService.conditionSearch(ordersPageQueryDTO);

        return Result.success(data);
    }

    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> orderStatistics() {
        // 日志记录
        log.info("订单各个状态数量统计");
        // 调用service完成统计
        OrderStatisticsVO data = orderService.orderStatistics();

        return Result.success(data);
    }

    @GetMapping("/details/{id}")
    public Result<OrderVO> orderDetail(@PathVariable Long id) {
        // 日志记录
        log.info("查询id为{}的订单", id);
        // 调用service完成查询
        OrderVO data = orderService.orderDetail(id);

        return Result.success(data);
    }
}
