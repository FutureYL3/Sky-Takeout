package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
@RestController("adminOrderController")
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

    @PutMapping("/confirm")
    public Result confirmOrder(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        Long id = ordersConfirmDTO.getId();
        // 日志记录
        log.info("商家接收订单号为{}的订单", id);
        // 调用service完成接单
        orderService.confirmOrder(id);

        return Result.success();
    }

    @PutMapping("/rejection")
    public Result rejectOrder(@RequestBody OrdersRejectionDTO ordersRejectionDTO) {
        // 日志记录
        log.info("拒绝订单：{}", ordersRejectionDTO);
        // 调用service进行拒单
        orderService.rejectOrder(ordersRejectionDTO);

        return Result.success();
    }

    @PutMapping("/cancel")
    public Result cancelOrder(@RequestBody OrdersCancelDTO ordersCancelDTO) {
        // 日志记录
        log.info("取消订单：{}", ordersCancelDTO);
        // 调用service完成取消
        orderService.cancelOrderAdmin(ordersCancelDTO);

        return Result.success();
    }

    @PutMapping("/delivery/{id}")
    public Result deliveryOrder(@PathVariable Long id) {
        // 日志记录
        log.info("开始派送id为{}的订单", id);
        // 调用service完成派送更新
        orderService.deliveryOrder(id);

        return Result.success();
    }

    @PutMapping("/complete/{id}")
    public Result completeOrder(@PathVariable Long id) {
        // 日志记录
        log.info("完成id为{}的订单", id);
        // 调用service完成派送更新
        orderService.completeOrder(id);

        return Result.success();
    }
}
