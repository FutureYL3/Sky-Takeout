package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.result.Result;
import com.sky.service.DishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: DishController
 * <p>
 * Package: com.sky.controller.admin
 * <p>
 * Description:
 * <p>
 *
 * @Author: yl
 * @Create: 2024/3/13 - 21:56
 * @Version: v1.0
 */
@RestController
@RequestMapping("/admin/dish")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DishController {

    private final DishService dishService;

    @PostMapping
    public Result save(@RequestBody DishDTO dto) {
        // 日志记录
        log.info("新增菜品：{}", dto);

        // 调用service完成新增菜品
        dishService.saveWithFlavor(dto);

        return Result.success();
    }
}
