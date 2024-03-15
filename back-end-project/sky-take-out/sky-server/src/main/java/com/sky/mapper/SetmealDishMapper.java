package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.annotation.AutoFill;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * ClassName: SetmealDishMapper
 * <p>
 * Package: com.sky.mapper
 * <p>
 * Description:
 * <p>
 *
 * @Author: yl
 * @Create: 2024/3/14 - 19:40
 * @Version: v1.0
 */
@Mapper
public interface SetmealDishMapper extends BaseMapper<SetmealDish> {
    @AutoFill(value = OperationType.INSERT)
    void addNew(Setmeal setmeal);

    List<Integer> getCorrespondDishesStatus(Long id);
}
