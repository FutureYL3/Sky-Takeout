package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * ClassName: ShopServiceImpl
 * <p>
 * Package: com.sky.service.impl
 * <p>
 * Description:
 * <p>
 *
 * @Author: yl
 * @Create: 2024/3/16 - 18:54
 * @Version: v1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ShopServiceImpl implements ShopService {

    private final RedisTemplate redisTemplate;

    @Override
    public void updateStatus(String status) {
        redisTemplate.opsForValue().set(StatusConstant.SHOP_STATUS, status);
    }

    @Override
    public Integer getStatus() {
        return Integer.parseInt(redisTemplate.opsForValue().get(StatusConstant.SHOP_STATUS).toString());
    }
}
