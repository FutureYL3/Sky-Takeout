package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.*;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import com.sky.service.DishFlavorService;
import com.sky.service.DishService;
import com.sky.service.SetMealService;
import com.sky.vo.DishVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ClassName: DishServiceImpl
 * <p>
 * Package: com.sky.service.impl
 * <p>
 * Description:
 * <p>
 *
 * @Author: yl
 * @Create: 2024/3/13 - 21:58
 * @Version: v1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    private final DishMapper dishMapper;
    private final DishFlavorService dishFlavorService;
    private final SetmealDishMapper setmealDishMapper;
    private final CategoryService categoryService;
    private final SetMealService setMealService;

    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        // 新增菜品
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dish.setStatus(StatusConstant.DISABLE);
        dishMapper.saveDish(dish);

        // 新增菜品对应口味
        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishId);
            }
            dishFlavorService.saveBatch(flavors);
        }
    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        // 拿到查询参数
        int page = dishPageQueryDTO.getPage();
        int pageSize = dishPageQueryDTO.getPageSize();
        // 设置分页对象
        PageHelper.startPage(page, pageSize);
//        Page<Dish> dishPage = Page.of(page, pageSize);
        // 设置查询wrapper
//        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<Dish>()
//                .eq(status != null, Dish::getStatus, status)
//                .eq(categoryId != null, Dish::getCategoryId, categoryId)
//                .like(name != null, Dish::getName, name);
        // 查询
        Page<DishVO> pageResult = dishMapper.pageQuery(dishPageQueryDTO);
//        Page<Dish> pageResult = page(dishPage, wrapper);
        // 拿到查询数据
        long total = pageResult.getTotal();
        List<DishVO> list = pageResult.getResult();
        // 查找分类名称并封装到vo中
//        ArrayList<DishVO> list = new ArrayList<>();
//        for (Dish dish : records) {
//            // 查找分类名称
//            Long dishCategoryId = dish.getCategoryId();
//            String categoryName = categoryService.getById(dishCategoryId).getName();
//            // 结果封装到vo中
//            DishVO dishVO = new DishVO();
//            BeanUtils.copyProperties(dish, dishVO);
//            dishVO.setCategoryName(categoryName);
//            list.add(dishVO);
//        }
        // 返回结果
        return PageResult.builder().total(total).records(list).build();
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        // 筛选出未起售、未被套餐关联的参评的id
        ArrayList<Long> validIds = new ArrayList<>();
        boolean isContainInvalidDish = false;
        for (Long id : ids) {
            // 添加未起售且未被套餐关联的菜品
            LambdaQueryWrapper<SetmealDish> setmealWrapper = new LambdaQueryWrapper<SetmealDish>()
                    .eq(SetmealDish::getDishId, id);
            if (Objects.equals(dishMapper.selectById(id).getStatus(), StatusConstant.DISABLE)
                && setmealDishMapper.selectCount(setmealWrapper) == 0) {
                validIds.add(id);
            } else {
                isContainInvalidDish = true;
            }
        }
        if (isContainInvalidDish) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_CANNOT_BE_DELETED);
        }
        // 删除菜品
        removeByIds(validIds);
        // 删除关联的口味数据
        LambdaQueryWrapper<DishFlavor> dishFlavorWrapper = new LambdaQueryWrapper<DishFlavor>()
                .in(DishFlavor::getDishId, validIds);
        dishFlavorService.remove(dishFlavorWrapper);
    }

    @Override
    public DishVO selectById(Long id) {
        DishVO dishVO = new DishVO();
        // 查该id菜品的信息
        Dish dish = dishMapper.selectById(id);
        BeanUtils.copyProperties(dish, dishVO);
        // 查该id菜品的分类名
        LambdaQueryWrapper<Category> categoryNameWrapper = new LambdaQueryWrapper<Category>()
                .eq(Category::getId, dish.getCategoryId());
        dishVO.setCategoryName(categoryService.getOne(categoryNameWrapper).getName());
        // 查该id菜品的口味
        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<DishFlavor>()
                .eq(DishFlavor::getDishId, id);
        dishVO.setFlavors(dishFlavorService.list(wrapper));

        return dishVO;
    }

    @Override
    @Transactional
    public void modifyById(DishDTO dishDTO) {
        // 修改菜品的信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.modifyById(dish);
        // 修改关联口味的信息
        List<DishFlavor> flavors = dishDTO.getFlavors();
        // 先统一删除原来的口味信息
        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<DishFlavor>()
                .eq(DishFlavor::getDishId, dishDTO.getId());
        dishFlavorService.remove(wrapper);
        // 添加修改后的口味信息
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishDTO.getId());
        }
        dishFlavorService.saveBatch(flavors);

    }

    @Override
    public List<Dish> getByCategoryId(Long categoryId) {
        // 构造查询条件wrapper
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<Dish>()
                .eq(Dish::getCategoryId, categoryId);
        return list(wrapper);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        // 菜品停售，则包含菜品的套餐同时停售。
        // 拿到包含该菜品的套餐id列表
        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<SetmealDish>()
                .eq(SetmealDish::getDishId, id);
        List<SetmealDish> setmealDishes = setmealDishMapper.selectList(wrapper);
        List<Long> idList = setmealDishes.stream()
                .map(SetmealDish::getId)
                .collect(Collectors.toList());
        // 更新这些套餐为停售
        LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<Setmeal>()
                .in(Setmeal::getId, idList)
                .set(Setmeal::getStatus, StatusConstant.DISABLE);
        setMealService.update(updateWrapper);
        // 菜品停售
        LambdaUpdateWrapper<Dish> dishUpdateWrapper = new LambdaUpdateWrapper<Dish>()
                .eq(Dish::getId, id)
                .set(status != null, Dish::getStatus, status);
        update(dishUpdateWrapper);
    }


}
