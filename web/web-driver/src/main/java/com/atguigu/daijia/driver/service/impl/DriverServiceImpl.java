package com.atguigu.daijia.driver.service.impl;

import com.atguigu.daijia.common.constant.RedisConstant;
import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.dispatch.client.NewOrderFeignClient;
import com.atguigu.daijia.driver.client.DriverInfoFeignClient;
import com.atguigu.daijia.driver.service.DriverService;
import com.atguigu.daijia.map.client.LocationFeignClient;
import com.atguigu.daijia.model.form.driver.DriverFaceModelForm;
import com.atguigu.daijia.model.form.driver.UpdateDriverAuthInfoForm;
import com.atguigu.daijia.model.vo.driver.DriverAuthInfoVo;
import com.atguigu.daijia.model.vo.driver.DriverLoginVo;
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

    @Autowired
    private LocationFeignClient locationFeignClient;

    @Autowired
    private NewOrderFeignClient newOrderFeignClient;

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
        redisTemplate.opsForValue().set(RedisConstant.USER_LOGIN_KEY_PREFIX + token, driverId.toString(),
                RedisConstant.USER_LOGIN_KEY_TIMEOUT, TimeUnit.SECONDS);

        //4.返回 token
        return token;
    }

    @Override
    public DriverLoginVo getDriverLoginInfo(Long driverId) {
        return driverInfoFeignClient.getDriverLoginInfo(driverId).getData();
    }

    @Override
    public DriverAuthInfoVo getDriverAuthInfo(Long driverId) {
        return driverInfoFeignClient.getDriverAuthInfo(driverId).getData();
    }

    @Override
    public Boolean updateDriverAuthInfo(UpdateDriverAuthInfoForm updateDriverAuthInfoForm) {
        return driverInfoFeignClient.updateDriverAuthInfo(updateDriverAuthInfoForm).getData();
    }

    @Override
    public Boolean createDriverFaceModel(DriverFaceModelForm driverFaceModelForm) {
        return driverInfoFeignClient.createDriverFaceModel(driverFaceModelForm).getData();
    }

    @Override
    public Boolean isFaceRecognition(Long driverId) {
        return driverInfoFeignClient.isFaceRecognition(driverId).getData();
    }

    @Override
    public Boolean verifyDriverFace(DriverFaceModelForm driverFaceModelForm) {
        return driverInfoFeignClient.verifyDriverFace(driverFaceModelForm).getData();
    }

    @Override
    public Boolean updateServiceStatus(Long driverId, Integer status) {
        return driverInfoFeignClient.updateServiceStatus(driverId, status).getData();
    }

    @Override
    public Boolean startService(Long driverId) {
        //判断认证状态
        DriverLoginVo driverLoginInfo = getDriverLoginInfo(driverId);
        if (driverLoginInfo.getAuthStatus() !=2) {
            throw new GuiguException(ResultCodeEnum.AUTH_ERROR);
        }
        //判断当日是否人脸识别
        Boolean faceRecognition = isFaceRecognition(driverId);
        if (faceRecognition) {
            throw new GuiguException(ResultCodeEnum.FACE_ERROR);
        }
        //更新司机接单状态(1.开始接单，2.关闭接单)
        updateServiceStatus(driverId, 1);
        //删除司机位置信息
        locationFeignClient.removeDriverLocation(driverId);
        //清空司机新订单队列
        newOrderFeignClient.clearNewOrderQueueData(driverId);
        return true;
    }



}
