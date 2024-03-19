package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.DishService;
import com.sky.service.SetMealService;
import com.sky.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ClassName: ShoppingCartServiceImpl
 * <p>
 * Package: com.sky.service.impl
 * <p>
 * Description:
 * <p>
 *
 * @Author: yl
 * @Create: 2024/3/18 - 22:36
 * @Version: v1.0
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
    private final SetMealService setMealService;
    private final DishService dishService;

    @Override
    public void addToCart(ShoppingCartDTO shoppingCartDTO) {
        // 判断当前加入购物车的商品是否已存在，已存在的话更新商品的数量+1
        Long id = BaseContext.getCurrentId();
        Long setmealId = shoppingCartDTO.getSetmealId();
        Long dishId = shoppingCartDTO.getDishId();
        String dishFlavor = shoppingCartDTO.getDishFlavor();
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<ShoppingCart>()
                .eq(id != null, ShoppingCart::getUserId, id)
                .eq(setmealId != null, ShoppingCart::getSetmealId, setmealId)
                .eq(dishId != null, ShoppingCart::getDishId, dishId)
                .eq(dishFlavor != null, ShoppingCart::getDishFlavor, dishFlavor);
        List<ShoppingCart> list = list(wrapper);
        if (list != null && !list.isEmpty()) {
            LambdaUpdateWrapper<ShoppingCart> updateWrapper = new LambdaUpdateWrapper<ShoppingCart>()
                    .eq(ShoppingCart::getId, list.get(0).getId())
                    .set(ShoppingCart::getNumber, list.get(0).getNumber() + 1);
            update(updateWrapper);
            return;
        }
        // 如果当前商品不存在，则插入到购物车表中，默认数量为1
        ShoppingCart shoppingCart = new ShoppingCart();
        if (setmealId != null && dishId == null) {
            Setmeal setmeal = setMealService.getById(setmealId);
            String name = setmeal.getName();
            String image = setmeal.getImage();
            BigDecimal price = setmeal.getPrice();
            shoppingCart = ShoppingCart.builder().userId(id).name(name).setmealId(setmealId)
                    .number(1).amount(price).image(image).createTime(LocalDateTime.now()).build();
        }
        if (setmealId == null && dishId != null) {
            Dish dish = dishService.getById(dishId);
            String name = dish.getName();
            String image = dish.getImage();
            BigDecimal price = dish.getPrice();
            shoppingCart = ShoppingCart.builder().userId(id).name(name).dishId(dishId)
                    .number(1).amount(price).image(image).dishFlavor(dishFlavor).createTime(LocalDateTime.now()).build();
        }
        save(shoppingCart);

    }

    @Override
    public void subFromCart(ShoppingCartDTO shoppingCartDTO) {
        // 拿到传递参数
        Long userId = BaseContext.getCurrentId();
        String dishFlavor = shoppingCartDTO.getDishFlavor();
        Long dishId = shoppingCartDTO.getDishId();
        Long setmealId = shoppingCartDTO.getSetmealId();
        // 删除，如果当前数量为1，则将该项从用户购物车中删除
        // 对于菜品删除
        if (dishId != null && setmealId == null) {
            // 拿到该购物车项
            LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<ShoppingCart>()
                    .eq(ShoppingCart::getDishId, dishId)
                    .eq(userId != null, ShoppingCart::getUserId, userId)
                    .eq(dishFlavor != null, ShoppingCart::getDishFlavor, dishFlavor);

            ShoppingCart shoppingCart = getOne(wrapper);
            // 数量为小于等于1，则删除
            if (shoppingCart.getNumber() <= 1) {
                remove(wrapper);
                return;
            }
            // 数量大于1，则修改
            LambdaUpdateWrapper<ShoppingCart> updateWrapper = new LambdaUpdateWrapper<ShoppingCart>()
                    .eq(ShoppingCart::getDishId, dishId)
                    .eq(userId != null, ShoppingCart::getUserId, userId)
                    .eq(dishFlavor != null, ShoppingCart::getDishFlavor, dishFlavor)
                    .set(ShoppingCart::getNumber, shoppingCart.getNumber() - 1);
            update(updateWrapper);
            return;
        }
        // 对于套餐删除
        if (setmealId != null && dishId == null) {
            // 业务逻辑同上
            LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<ShoppingCart>()
                    .eq(userId != null, ShoppingCart::getUserId, userId)
                    .eq(ShoppingCart::getSetmealId, setmealId);
            ShoppingCart shoppingCart = getOne(wrapper);
            if (shoppingCart.getNumber() <= 1) {
                remove(wrapper);
                return;
            }

            LambdaUpdateWrapper<ShoppingCart> updateWrapper = new LambdaUpdateWrapper<ShoppingCart>()
                    .eq(userId != null, ShoppingCart::getUserId, userId)
                    .eq(ShoppingCart::getSetmealId, setmealId)
                    .set(ShoppingCart::getNumber, shoppingCart.getNumber() - 1);
            update(updateWrapper);
        }
    }
}
