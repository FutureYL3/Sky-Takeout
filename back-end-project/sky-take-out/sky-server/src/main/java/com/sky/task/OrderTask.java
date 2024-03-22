package com.sky.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ClassName: task
 * <p>
 * Package: com.sky
 * <p>
 * Description:
 * <p>
 *
 * @Author: yl
 * @Create: 2024/3/22 - 15:35
 * @Version: v1.0
 */

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OrderTask {
    private final OrderService orderService;

    /**
     * 处理超时订单
     */
    // 每分钟触发一次
    @Scheduled(cron = "0 0/1 * * * ?")
    public void handleTimeoutOrder() {
        // 拿到全部超时未付款的订单的id
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<Orders>()
                // 超时的订单
                .le(Orders::getOrderTime, LocalDateTime.now().minusMinutes(15))
                // 未付款的订单
                .eq(Orders::getStatus, Orders.PENDING_PAYMENT);
        List<Long> ids = orderService.list(wrapper).stream().map(Orders::getId).collect(Collectors.toList());
        // 更新这些订单的状态为已取消
        orderService.cancelTimeoutOrder(ids);
    }

    /**
     * 处理派送中订单
     */
    // 每天凌晨一点触发一次
    @Scheduled(cron = "0 0 1 * * ?")
    public void handleDeliveryInProgressOrder() {
        // 拿到全部处于派送中状态的订单的id
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<Orders>()
                // 派送中的订单
                .eq(Orders::getStatus, Orders.DELIVERY_IN_PROGRESS)
                // 前一天的订单
                .ge(Orders::getOrderTime, LocalDateTime.now().minusDays(1));
        List<Long> ids = orderService.list(wrapper).stream().map(Orders::getId).collect(Collectors.toList());
        // 更新这些订单的状态为已完成
        orderService.completeDeliveryInProgressOrder(ids);
    }
}
