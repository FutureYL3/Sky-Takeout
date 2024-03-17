package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: ShopController
 * <p>
 * Package: com.sky.controller.admin
 * <p>
 * Description:
 * <p>
 *
 * @Author: yl
 * @Create: 2024/3/16 - 18:52
 * @Version: v1.0
 */
@RestController("adminShopController")
@Slf4j
@RequestMapping("/admin/shop")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ShopController {

    private final ShopService shopService;

    @PutMapping("/{status}")
    public Result updateStatus(@PathVariable Integer status) {
        // 日志记录
        log.info("将店铺的营业状态设置为：{}", status);
        // 调用service完成状态更新
        shopService.updateStatus(status.toString());

        return Result.success();
    }

    @GetMapping("/status")
    public Result<Integer> getStatus() {
        // 日志记录
        log.info("管理端获取店铺状态");
        // 调用service获取状态
        Integer status = shopService.getStatus();

        return Result.success(status);
    }
}
