package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.OutputBuffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: OrderController
 * <p>
 * Package: com.sky.controller.user
 * <p>
 * Description:
 * <p>
 *
 * @Author: yl
 * @Create: 2024/3/20 - 16:14
 * @Version: v1.0
 */

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        // 日志记录
        log.info("提交订单：{}", ordersSubmitDTO);
        // 调用service完成提交
        OrderSubmitVO orderSubmitVO = orderService.submit(ordersSubmitDTO);

        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    @GetMapping("/historyOrders")
    public Result<PageResult> historyOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 日志记录
        log.info("用户查询历史订单：{}", ordersPageQueryDTO);
        // 调用service完成查询
        PageResult data = orderService.historyOrders(ordersPageQueryDTO);

        return Result.success(data);
    }

    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> getById(@PathVariable Long id) {
        // 日志记录
        log.info("查询id为：{}的订单", id);
        // 调用service完成查询
        OrderVO orderVO = orderService.selectById(id);

        return Result.success(orderVO);
    }

    @PostMapping("/repetition/{id}")
    public Result anotherOrder(@PathVariable Long id) {
        // 日志记录
        log.info("用户再来一单订单id为{}的订单", id);
        // 调用service完成再来一单
        orderService.anotherOrder(id);

        return Result.success();
    }

    @PutMapping("/cancel/{id}")
    public Result cancelOrder(@PathVariable Long id) {
        // 日志记录
        log.info("取消id为{}的订单", id);
        // 调用service完成取消
        orderService.cancelOrder(id);

        return Result.success();
    }
}
