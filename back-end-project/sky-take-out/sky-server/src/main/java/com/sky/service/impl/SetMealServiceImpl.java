package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetMealService;
import com.sky.service.SetmealDishService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ClassName: SetMealServiceImpl
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
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SetMealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetMealService {
    private final SetmealDishMapper setmealDishMapper;
    private final SetmealDishService setmealDishService;
    private final SetmealMapper setmealMapper;
    @Override
    public void addNew(SetmealDTO setmealDTO) {
        // 新增套餐
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmeal.setStatus(StatusConstant.DISABLE);
        setmealDishMapper.addNew(setmeal);
        // 维护套餐和菜品的关系
        Long setmealId = setmeal.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealId);
        }
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        // 拿到查询参数
        int page = setmealPageQueryDTO.getPage();
        int pageSize = setmealPageQueryDTO.getPageSize();
        // 设置分页插件
        PageHelper.startPage(page, pageSize);
        // 分页查询
        Page<SetmealVO> pageResult = setmealMapper.pageQuery(setmealPageQueryDTO);
        // 拿到查询数据
        long total = pageResult.getTotal();
        List<SetmealVO> data = pageResult.getResult();
        return PageResult.builder().total(total).records(data).build();
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 拿到可以删除的套餐的id
        boolean isContainOnSaleSetmeal = false;
        ArrayList<Long> validIds = new ArrayList<>();
        for (Long id : ids) {
            if (Objects.equals(getById(id).getStatus(), StatusConstant.DISABLE)) {
                validIds.add(id);
            } else {
                isContainOnSaleSetmeal = true;
            }
        }
        // 如果有在售的套餐，则抛异常
        if (isContainOnSaleSetmeal) {
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }
        // 删除可删除的套餐信息
        removeBatchByIds(validIds);
        // 删除套餐关联菜品信息
        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<SetmealDish>()
                .in(SetmealDish::getSetmealId, validIds);
        setmealDishService.remove(wrapper);

    }

    @Override
    public SetmealVO queryById(Long id) {
        // 拿到套餐基本信息，包括分类名
        SetmealVO setmealVO = setmealMapper.getById(id);
        // 拿到套餐关联菜品信息
        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<SetmealDish>()
                .eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishes = setmealDishService.list(wrapper);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    @Override
    @Transactional
    public void modify(SetmealDTO setmealDTO) {
        // 更新套餐信息，传入setmeal以进行字段自动填充
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.modify(setmeal);
        // 更新关联菜品信息，先删除原来菜品，再添加修改后的菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        // 填充套餐id
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealDTO.getId());
        }
        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<SetmealDish>()
                .eq(SetmealDish::getSetmealId, setmealDTO.getId());
        setmealDishService.remove(wrapper);
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        // 套餐内有停售菜品，无法起售
        if (Objects.equals(status, StatusConstant.ENABLE)) {
            // 拿到关联菜品的状态列表
            List<Integer> dishStatus = setmealDishMapper.getCorrespondDishesStatus(id);
            if (dishStatus.contains(StatusConstant.DISABLE)) {
                throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }
        }

        // 无停售菜品，更新状态
        LambdaUpdateWrapper<Setmeal> wrapper = new LambdaUpdateWrapper<Setmeal>()
                .eq(Setmeal::getId, id)
                .set(status != null, Setmeal::getStatus, status);
        update(wrapper);
    }

    @Override
    public List<Setmeal> listByCategoryId(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<Setmeal>()
                .eq(Setmeal::getCategoryId, setmeal.getCategoryId())
                .eq(Setmeal::getStatus, StatusConstant.ENABLE);
        return list(wrapper);
    }

    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishesById(id);
    }
}
