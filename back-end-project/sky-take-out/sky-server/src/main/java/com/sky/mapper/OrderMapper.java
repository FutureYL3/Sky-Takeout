package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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

    void cancelOrder(Long id);

    Page<OrderVO> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);
}
