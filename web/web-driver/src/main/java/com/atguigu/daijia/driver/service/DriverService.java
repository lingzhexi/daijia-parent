package com.atguigu.daijia.driver.service;

import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.model.form.driver.DriverFaceModelForm;
import com.atguigu.daijia.model.form.driver.UpdateDriverAuthInfoForm;
import com.atguigu.daijia.model.vo.driver.DriverAuthInfoVo;
import com.atguigu.daijia.model.vo.driver.DriverLoginVo;

public interface DriverService {

    String login(String code);

    DriverLoginVo getDriverLoginInfo(Long driverId);

    DriverAuthInfoVo getDriverAuthInfo(Long driverId);

    Boolean updateDriverAuthInfo(UpdateDriverAuthInfoForm updateDriverAuthInfoForm);

    Boolean createDriverFaceModel(DriverFaceModelForm driverFaceModelForm);

    Boolean isFaceRecognition(String driverId);


    Boolean verifyDriverFace(DriverFaceModelForm driverFaceModelForm);

    Boolean updateServiceStatus(Long driverId, Integer status);
}
