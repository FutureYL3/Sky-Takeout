package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: ShopController
 * <p>
 * Package: com.sky.controller.user
 * <p>
 * Description:
 * <p>
 *
 * @Author: yl
 * @Create: 2024/3/16 - 19:00
 * @Version: v1.0
 */
@RestController("userShopController")
@RequestMapping("/user/shop")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ShopController {

    private final ShopService shopService;

    @GetMapping("/status")
    public Result<Integer> getStatus() {
        // 日志记录
        log.info("用户端获取店铺状态");
        // 调用service获取状态
        Integer status = shopService.getStatus();

        return Result.success(status);
    }
}
