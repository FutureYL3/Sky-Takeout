package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

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

    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void batchDelete(List<Long> ids);

    DishVO selectById(Long id);

    void modifyById(DishDTO dishDTO);

    List<Dish> getByCategoryId(Long categoryId);

    void updateStatus(Long id, Integer status);

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}
