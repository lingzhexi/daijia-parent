package com.atguigu.daijia.driver.service.impl;

import com.atguigu.daijia.common.constant.RedisConstant;
import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.driver.client.DriverInfoFeignClient;
import com.atguigu.daijia.driver.service.DriverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class DriverServiceImpl implements DriverService {

    @Autowired
    private DriverInfoFeignClient driverInfoFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    //登陆
    @Override
    public String login(String code) {
        //1.远程调用获取司机id
        Result<Long> codeResult = driverInfoFeignClient.login(code);
        if (codeResult.getCode() != 200) {
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        Long driverId = codeResult.getData();
        //2.生成token
        String token = UUID.randomUUID().toString().replaceAll("-", "");

        //3.token 存入 redis 设置过期时间
        redisTemplate.opsForValue().set(RedisConstant.USER_LOGIN_KEY_PREFIX + token, driverId,
                RedisConstant.USER_LOGIN_KEY_TIMEOUT, TimeUnit.MICROSECONDS);

        //4.返回 token
        return token;
    }
}
