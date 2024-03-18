package com.sky.controller.admin;


import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetMealService;
import com.sky.vo.SetmealVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName: SetmealController
 * <p>
 * Package: com.sky.controller.admin
 * <p>
 * Description:
 * <p>
 *
 * @Author: yl
 * @Create: 2024/3/14 - 21:09
 * @Version: v1.0
 */
@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SetmealController {

    private final SetMealService setMealService;

    @PostMapping
    @CacheEvict(cacheNames = "setmealCache", key = "#setmealDTO.categoryId")
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        // 日志记录
        log.info("新增套餐：{}", setmealDTO);
        // 调用service完成新增
        setMealService.addNew(setmealDTO);

        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        // 日志记录
        log.info("查询套餐：{}", setmealPageQueryDTO);
        // 调用service完成查询
        PageResult data = setMealService.pageQuery(setmealPageQueryDTO);

        return Result.success(data);
    }

    @DeleteMapping
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result delete(@RequestParam List<Long> ids) {
        // 日志记录
        log.info("删除id为{}的套餐", ids);
        // 调用service来删除套餐
        setMealService.deleteBatch(ids);

        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        // 日志记录
        log.info("查询id为{}的套餐", id);
        // 调用service完成查询
        SetmealVO setmealVO = setMealService.queryById(id);

        return Result.success(setmealVO);
    }

    @PutMapping
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        // 日志记录
        log.info("修改套餐：{}", setmealDTO);
        // 调用service完成修改
        setMealService.modify(setmealDTO);

        return Result.success();
    }

    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result updateStatus(@PathVariable Integer status, Long id) {
        // 日志记录
        log.info("修改id为{}的套餐状态为{}", id, status);
        // 调用service完成修改
        setMealService.updateStatus(id, status);

        return Result.success();
    }
}
