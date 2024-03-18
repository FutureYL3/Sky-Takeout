package com.sky.controller.user;

import com.alibaba.fastjson.JSON;
import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DishController {
    private final DishService dishService;
    private final RedisTemplate redisTemplate;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        // 构造key
        String key = "dish_" + categoryId;
        // 查询redis中是否存在缓存数据
        String list = (String) redisTemplate.opsForValue().get(key);
        List<DishVO> data = JSON.parseArray(list, DishVO.class);
        // 存在缓存数据，直接返回给前端
        if (data != null && !data.isEmpty()) {
            return Result.success(data);
        }
        // 不存在缓存数据，则查询数据库，并添加到redis中
        // 查询
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品

        data = dishService.listWithFlavor(dish);
        // 添加
        redisTemplate.opsForValue().set(key, JSON.toJSONString(data));
        // 返回
        return Result.success(data);
    }

}
