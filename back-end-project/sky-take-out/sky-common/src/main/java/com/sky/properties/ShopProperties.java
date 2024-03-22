package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ClassName: ShopProperties
 * <p>
 * Package: com.sky.properties
 * <p>
 * Description:
 * <p>
 *
 * @Author: yl
 * @Create: 2024/3/21 - 19:19
 * @Version: v1.0
 */
@Component
@ConfigurationProperties(prefix = "sky.shop")
@Data
public class ShopProperties {
    private String address;
}
