package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.vo.UserLoginVO;

public interface UserService extends IService<User> {
    UserLoginVO login(UserLoginDTO userLoginDTO);
}
