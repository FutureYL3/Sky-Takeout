package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.properties.ShopProperties;
import com.sky.result.PageResult;
import com.sky.service.OrderDetailService;
import com.sky.service.OrderService;
import com.sky.service.ShoppingCartService;
import com.sky.service.UserService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * ClassName: OrderServiceImpl
 * <p>
 * Package: com.sky.service.impl
 * <p>
 * Description:
 * <p>
 *
 * @Author: yl
 * @Create: 2024/3/20 - 16:18
 * @Version: v1.0
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {
    private final UserService userService;
    private final OrderDetailService orderDetailService;
    private final AddressBookMapper addressBookMapper;
    private final ShoppingCartService shoppingCartService;
    private final WeChatPayUtil weChatPayUtil;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final ShopProperties shopProperties;
    private final WebSocketServer webSocketServer;
    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        // 各种业务异常
        // 地址薄为空
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        AddressBook address = addressBookMapper.getById(addressBookId);
        if (address == null) {
            throw new OrderBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        // 购物车数据为空
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<ShoppingCart>()
                .eq(userId != null, ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(wrapper);
        if (shoppingCarts == null || shoppingCarts.isEmpty()) {
            throw new OrderBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        // 判断用户收获地址是否在五公里内
        // 拿到用户的收货地址的经纬度

        // 拿到商家位置的经纬度

        // 计算用户和商家之间的直线距离

        // 判断，若超过五公里，则抛异常

        // 向订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setUserName(userService.getById(userId).getName());
        orders.setPhone(address.getPhone());
        orders.setAddress(address.getDetail());
        orders.setConsignee(address.getConsignee());

        save(orders);
        // 向订单明细表插入多条数据
        Long ordersId = orders.getId();
        ArrayList<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart shoppingCart : shoppingCarts) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            orderDetail.setOrderId(ordersId);
            orderDetails.add(orderDetail);
        }
        orderDetailService.saveBatch(orderDetails);

        // 清空购物车
        shoppingCartService.remove(wrapper);
        // 封装返回结果
        return OrderSubmitVO.builder().id(ordersId).orderAmount(ordersSubmitDTO
                .getAmount()).orderNumber(orders.getNumber()).orderTime(orders.getOrderTime()).build();
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.selectById(userId);

        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
        JSONObject jsonObject = new JSONObject();
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));
        paySuccess(ordersPaymentDTO.getOrderNumber());
        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
        // 来单提醒
        HashMap<String, Object> map = new HashMap<>();
        map.put("type", 1);
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号：" + outTradeNo);
        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
    }

    @Override
    public PageResult historyOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 填充dto的userid
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        // 构造返回的List集合
        List<OrderVO> records;
        // 查询该用户的订单
        int page = ordersPageQueryDTO.getPage();
        int pageSize = ordersPageQueryDTO.getPageSize();
        PageHelper.startPage(page, pageSize);
        // 拿到没有订单详情的OrderVO
        Page<OrderVO> result = orderMapper.historyOrdersWithoutDetail(ordersPageQueryDTO);
        // 拿到总数
        long total = result.getTotal();
        // 拿到List
        records = result.getResult();
        // 为List中每个订单查询出其订单详情
        for (OrderVO orderVO : records) {
            Long id = orderVO.getId();
            LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<OrderDetail>()
                    .eq(OrderDetail::getOrderId, id);
            orderVO.setOrderDetailList(orderDetailService.list(wrapper));
        }

        // 返回分页查询结果
        return new PageResult(total, records);

    }

    @Override
    public OrderVO selectById(Long orderId) {
        // 拿到订单基本信息
        return getOrderVO(orderId);
    }

    @Override
    public void anotherOrder(Long orderId) {
        // 拿到该订单的所有订单详情
        LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, orderId);
        List<OrderDetail> details = orderDetailService.list(wrapper);
        // 新建一个购物车List
        ArrayList<ShoppingCart> shoppingCarts = new ArrayList<>();
        // 将订单详情中的菜品再次封装到购物车中
        for (OrderDetail orderDetail : details) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCarts.add(shoppingCart);
        }
        // 新增到购物车中
        shoppingCartService.saveBatch(shoppingCarts);
    }

    @Override
    public void cancelOrder(Long id) {
        // 拿到订单
        Orders orders = getById(id);
        // 判断订单状态
        Integer status = orders.getStatus();
        // 状态为已接单或派送中，则弹出信息
        if (Objects.equals(status, Orders.CONFIRMED) || Objects.equals(status, Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_CANNOT_CANCEL);
        }
        // 状态为待付款或待接单，可直接取消
        if (Objects.equals(status, Orders.PENDING_PAYMENT) || Objects.equals(status, Orders.TO_BE_CONFIRMED)) {
            // Framework Gap !!!!!!!!!
//            LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<Orders>()
//                    .eq(Orders::getId, id)
//                    .set(Orders::getStatus, Orders.CANCELLED);
//            update(updateWrapper);
            orderMapper.cancelOrder(id, LocalDateTime.now());
        }
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        int page = ordersPageQueryDTO.getPage();
        int pageSize = ordersPageQueryDTO.getPageSize();
        PageHelper.startPage(page, pageSize);
        // 查询
        Page<OrderVO> result = orderMapper.conditionSearch(ordersPageQueryDTO);
        long total = result.getTotal();
        List<OrderVO> records = result.getResult();

        return new PageResult(total, records);

    }

    @Override
    public OrderStatisticsVO orderStatistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        Integer[] statusArray = {Orders.TO_BE_CONFIRMED, Orders.CONFIRMED, Orders.DELIVERY_IN_PROGRESS};
        for (Integer status : statusArray) {
            LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<Orders>()
                    .eq(Orders::getStatus, status);
            Integer count = Math.toIntExact(count(wrapper));
            switch (status) {
                case 2:
                    orderStatisticsVO.setToBeConfirmed(count);
                    break;
                case 3:
                    orderStatisticsVO.setConfirmed(count);
                    break;
                case 4:
                    orderStatisticsVO.setDeliveryInProgress(count);
                    break;
            }
        }

        return orderStatisticsVO;
    }

    @Override
    public OrderVO orderDetail(Long id) {
        return getOrderVO(id);
    }

    @Override
    public void confirmOrder(Long id) {
        // 拿到订单信息
        Orders orders = getById(id);
        // 如果订单状态为待接单，则更改订单状态为已接单（3）
        if (Objects.equals(orders.getStatus(), Orders.TO_BE_CONFIRMED)) {
            orderMapper.confirmOrder(id);
        }
    }

    @Override
    public void rejectOrder(OrdersRejectionDTO ordersRejectionDTO) {
        // 订单只有在待处理状态才能拒单
        Orders orders = getById(ordersRejectionDTO.getId());
        if (Objects.equals(orders.getStatus(), Orders.TO_BE_CONFIRMED)) {
            orderMapper.rejectOrder(ordersRejectionDTO, LocalDateTime.now());
        }
    }

    @Override
    public void cancelOrderAdmin(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = getById(ordersCancelDTO.getId());
        // 待付款、待派送、派送中、已完成状态可以进行取消操作
        if (!Objects.equals(orders.getStatus(), Orders.TO_BE_CONFIRMED)
                || !orders.getStatus().equals(Orders.CANCELLED)) {
            orderMapper.cancelOrderAdmin(ordersCancelDTO, LocalDateTime.now());
        }
    }

    @Override
    public void deliveryOrder(Long id) {
        // 只有待派送的订单才能进行派送操作
        Orders orders = getById(id);
        if (Objects.equals(orders.getStatus(), Orders.CONFIRMED)) {
            orderMapper.deliveryOrder(id);
        }
    }

    @Override
    public void completeOrder(Long id) {
        // 只有派送中的订单才能进行完成操作
        Orders orders = getById(id);
        if (Objects.equals(orders.getStatus(), Orders.DELIVERY_IN_PROGRESS)) {
            orderMapper.completeOrder(id, LocalDateTime.now());
        }
    }

    @Override
    public void cancelTimeoutOrder(List<Long> ids) {
        orderMapper.cancelTimeoutOrder(ids, LocalDateTime.now());
    }

    @Override
    public void completeDeliveryInProgressOrder(List<Long> ids) {
        orderMapper.completeDeliveryInProgressOrder(ids);
    }

    @Override
    public void remindOrder(Long id) {
        Orders orders = getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("type", 2);
        map.put("orderId", id);
        map.put("content", "订单号：" + orders.getNumber());
        String jsonString = JSON.toJSONString(map);

        webSocketServer.sendToAllClient(jsonString);
    }

    private OrderVO getOrderVO(Long id) {
        Orders orders = getById(id);
        LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, id);
        List<OrderDetail> details = orderDetailService.list(wrapper);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(details);
        return orderVO;
    }


}
