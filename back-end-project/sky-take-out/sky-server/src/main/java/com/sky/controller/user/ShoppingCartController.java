package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
