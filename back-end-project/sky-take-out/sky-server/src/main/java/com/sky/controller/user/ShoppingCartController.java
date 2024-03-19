package com.sky.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName: ShoppingCartController
 * <p>
 * Package: com.sky.controller.user
 * <p>
 * Description:
 * <p>
 *
 * @Author: yl
 * @Create: 2024/3/18 - 22:34
 * @Version: v1.0
 */
@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public Result addToCart(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        // 日志记录
        log.info("添加到购物车：{}", shoppingCartDTO);
        // 调用service完成添加
        shoppingCartService.addToCart(shoppingCartDTO);

        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<ShoppingCart>> list() {
        // 日志记录
        log.info("查询购物车");
        // 拿到用户openid
        Long openId = BaseContext.getCurrentId();
        // 查询数据库
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, openId);
        List<ShoppingCart> data = shoppingCartService.list(wrapper);

        return Result.success(data);
    }

    @DeleteMapping("/clean")
    public Result deleteAll() {
        // 日志记录
        log.info("清空购物车");
        // 拿到用户openid
        Long openId = BaseContext.getCurrentId();
        // 清空购物车
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, openId);
        shoppingCartService.remove(wrapper);

        return Result.success();
    }

    @PostMapping("/sub")
    public Result subFromCart(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        // 日志记录
        log.info("删除购物车中的商品：{}", shoppingCartDTO);
        // 调用service完成删除
        shoppingCartService.subFromCart(shoppingCartDTO);

        return Result.success();
    }
}
