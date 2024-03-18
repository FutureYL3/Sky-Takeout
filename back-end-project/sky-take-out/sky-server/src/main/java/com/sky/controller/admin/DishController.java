package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        // 日志记录
        log.info("分页查询菜品：{}", dishPageQueryDTO);
        // 调用service完成分页查询
        PageResult data = dishService.pageQuery(dishPageQueryDTO);

        return Result.success(data);
    }

    @DeleteMapping
    public Result page(@RequestParam List<Long> ids) {
        // 日志记录
        log.info("删除id为{}的菜品", ids);
        // 调用service删除菜品
        dishService.batchDelete(ids);

        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id) {
        // 日志记录
        log.info("查询id为{}的菜品", id);
        // 调用service完成查询
        DishVO dishVO = dishService.selectById(id);

        return Result.success(dishVO);
    }

    @PutMapping
    public Result updateById(@RequestBody DishDTO dishDTO) {
        // 日志记录
        log.info("更新菜品：{}", dishDTO);
        // 调用service完成修改
        dishService.modifyById(dishDTO);

        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<Dish>> getByCategoryId(Long categoryId) {
        // 日志记录
        log.info("查询分类id为{}下的菜品", categoryId);
        // 调用service查询菜品
        List<Dish> data = dishService.getByCategoryId(categoryId);

        return Result.success(data);
    }

    @PostMapping("/status/{status}")
    public Result updateStatus(Long id, @PathVariable Integer status) {
        // 日志记录
        log.info("更新id为{}的菜品状态为{}", id, status);
        // 调用service完成更新
        dishService.updateStatus(id, status);

        return Result.success();
    }

    // 清理redis缓存方法
}
