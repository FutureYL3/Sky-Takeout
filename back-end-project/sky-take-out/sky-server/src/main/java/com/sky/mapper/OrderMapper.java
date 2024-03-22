package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.Page;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ClassName: OrderMapper
 * <p>
 * Package: com.sky.mapper
 * <p>
 * Description:
 * <p>
 *
 * @Author: yl
 * @Create: 2024/3/20 - 16:18
 * @Version: v1.0
 */
@Mapper
public interface OrderMapper extends BaseMapper<Orders> {

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    Page<OrderVO> historyOrdersWithoutDetail(OrdersPageQueryDTO ordersPageQueryDTO);

    void cancelOrder(Long id, LocalDateTime time);

    Page<OrderVO> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    @Update("UPDATE orders SET status = 3 WHERE id = #{id}")
    void confirmOrder(Long id);

    @Update("UPDATE orders SET status = 6, rejection_reason = #{rejectionReason}, cancel_time = #{time} WHERE id = #{id}")
    void rejectOrder(OrdersRejectionDTO ordersRejectionDTO, LocalDateTime time);

    @Update("UPDATE orders SET status = 6, cancel_reason = #{cancelReason}, cancel_time = #{time} WHERE id = #{id}")
    void cancelOrderAdmin(OrdersCancelDTO ordersCancelDTO, LocalDateTime time);

    @Update("UPDATE orders SET status = 4 WHERE id = #{id} ")
    void deliveryOrder(Long id);

    @Update("UPDATE orders SET status = 5, delivery_time = #{time} WHERE id = #{id} ")
    void completeOrder(Long id, LocalDateTime time);

    void cancelTimeoutOrder(List<Long> ids, LocalDateTime time);

    void completeDeliveryInProgressOrder(List<Long> ids);
}
