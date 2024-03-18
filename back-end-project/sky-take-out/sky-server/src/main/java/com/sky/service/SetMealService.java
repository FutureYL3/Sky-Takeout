package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

/**
 * ClassName: SetMealService
 * <p>
 * Package: com.sky.service
 * <p>
 * Description:
 * <p>
 *
 * @Author: yl
 * @Create: 2024/3/14 - 21:36
 * @Version: v1.0
 */
public interface SetMealService extends IService<Setmeal> {
    void addNew(SetmealDTO setmealDTO);

    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    void deleteBatch(List<Long> ids);

    SetmealVO queryById(Long id);

    void modify(SetmealDTO setmealDTO);

    void updateStatus(Long id, Integer status);

    List<Setmeal> listByCategoryId(Setmeal setmeal);

    List<DishItemVO> getDishItemById(Long id);
}
