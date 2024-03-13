package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;

import java.util.PrimitiveIterator;

/**
 * ClassName: DishService
 * <p>
 * Package: com.sky.service
 * <p>
 * Description:
 * <p>
 *
 * @Author: yl
 * @Create: 2024/3/13 - 21:58
 * @Version: v1.0
 */
public interface DishService extends IService<Dish> {
    void saveWithFlavor(DishDTO dishDTO);
}
