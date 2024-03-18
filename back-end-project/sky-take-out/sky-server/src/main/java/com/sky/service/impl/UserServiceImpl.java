package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.JwtProperties;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final UserMapper userMapper;
    private final JwtProperties jwtProperties;
    private final WeChatProperties weChatProperties;
    private final static String LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";
    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        // 通过前端code获得用户的openid（微信用户唯一标识）
        String openid = getOpenid(userLoginDTO.getCode());
        // 验证openid是否为空，为空则传递参数code有误，登录失败
        if (openid == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        // 获取数据库中的该user信息
        User user = userMapper.getByOpenId(openid);
        // 如果user为空，则该用户为新用户，为其注册
        if (user == null) {
            user = User.builder().openid(openid).createTime(LocalDateTime.now()).build();
            save(user);
        }
        // 生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        // 返回UserLoginVO对象
        return UserLoginVO.builder().id(user.getId()).openid(user.getOpenid()).token(token).build();
    }

    private String getOpenid(String code) {
        // 通过前端code获得用户的openid（微信用户唯一标识）
        HashMap<String, String> params = new HashMap<>();
        params.put("appid", weChatProperties.getAppid());
        params.put("secret", weChatProperties.getSecret());
        params.put("js_code", code);
        params.put("grant_type", "authorization_code");
        String response = HttpClientUtil.doGet(LOGIN_URL, params);

        JSONObject jsonObject = JSON.parseObject(response);
        return jsonObject.getString("openid");
    }
}
