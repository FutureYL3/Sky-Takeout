package com.sky.service;

/**
 * ClassName: ShopService
 * <p>
 * Package: com.sky.service
 * <p>
 * Description:
 * <p>
 *
 * @Author: yl
 * @Create: 2024/3/16 - 18:53
 * @Version: v1.0
 */
public interface ShopService {
    void updateStatus(String status);

    Integer getStatus();
}
