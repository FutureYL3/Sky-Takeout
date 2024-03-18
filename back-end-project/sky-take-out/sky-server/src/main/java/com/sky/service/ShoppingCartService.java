package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

/**
 * ClassName: ShoppingCartService
 * <p>
 * Package: com.sky.service
 * <p>
 * Description:
 * <p>
 *
 * @Author: yl
 * @Create: 2024/3/18 - 22:35
 * @Version: v1.0
 */
public interface ShoppingCartService extends IService<ShoppingCart> {
    void addToCart(ShoppingCartDTO shoppingCartDTO);
}
