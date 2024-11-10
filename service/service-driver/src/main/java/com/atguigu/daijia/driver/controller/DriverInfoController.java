package com.atguigu.daijia.driver.controller;

import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.driver.service.DriverInfoService;
import com.atguigu.daijia.model.entity.driver.DriverSet;
import com.atguigu.daijia.model.form.driver.DriverFaceModelForm;
import com.atguigu.daijia.model.form.driver.UpdateDriverAuthInfoForm;
import com.atguigu.daijia.model.vo.driver.DriverAuthInfoVo;
import com.atguigu.daijia.model.vo.driver.DriverLoginVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "司机API接口管理")
@RestController
@RequestMapping(value = "/driver/info")
@SuppressWarnings({"unchecked", "rawtypes"})
public class DriverInfoController {

    @Autowired
    private DriverInfoService driverInfoService;

    // 微信小程序登录接口
    @Operation(summary = "小程序授权登录")
    @GetMapping("/login/{code}")
    public Result<Long> login(@PathVariable String code) {
        return Result.ok(driverInfoService.login(code));
    }

    @Operation(summary = "获取用户登录信息")
    @GetMapping("/getDriverLoginInfo/{driverId}")
    public Result<DriverLoginVo> getDriverLoginInfo(@PathVariable Long driverId) {
        return Result.ok(driverInfoService.getDriverLoginInfo(driverId));
    }

    @Operation(summary = "获取司机认证信息")
    @GetMapping("/getDriverAuthInfo/{driverId}")
    public Result<DriverAuthInfoVo> getDriverAuthInfo(@PathVariable("driverId") Long driverId) {
        return Result.ok(driverInfoService.getDriverAuthInfo(driverId));
    }

    @Operation(summary = "更新司机认证信息")
    @PostMapping("/updateDriverAuthInfo")
    public Result<Boolean> updateDriverAuthInfo(@RequestBody UpdateDriverAuthInfoForm updateDriverAuthInfoForm) {
        return Result.ok(driverInfoService.updateDriverAuthInfo(updateDriverAuthInfoForm));
    }

    @Operation(summary = "创建司机人脸模型")
    @PostMapping("/createDriverFaceModel")
    public Result<Boolean> createDriverFaceModel(@RequestBody DriverFaceModelForm driverFaceModelForm) {
        return Result.ok(driverInfoService.createDriverFaceModel(driverFaceModelForm));
    }

    @Operation(summary = "获取司机个性化设置")
    @GetMapping("/getDriverSet/{driverId}")
    public Result<DriverSet> getDriverSet(@PathVariable Long driverId) {
        return Result.ok(driverInfoService.getDriverSet(driverId));
    }

    @Operation(summary = "判断司机当日是否进行过人脸识别")
    @GetMapping("/isFaceRecognition/{driverId}")
    public Result<Boolean> isFaceRecognition(@PathVariable("driverId") String driverId) {
        return Result.ok(driverInfoService.isFaceRecognition(driverId));
    }

    @Operation(summary = "验证司机人脸")
    @PostMapping("/verifyDriverFace")
    public Result<Boolean> verifyDriverFace(@RequestBody DriverFaceModelForm driverFaceModelForm) {
        return Result.ok(driverInfoService.verifyDriverFace(driverFaceModelForm));
    }

    @Operation(summary = "更新接单状态")
    @GetMapping("/updateServiceStatus/{driverId}/{status}")
    public Result<Boolean> updateServiceStatus(@PathVariable("driverId") Long driverId, @PathVariable("status") Integer status) {
        return Result.ok(driverInfoService.updateServiceStatus(driverId, status));
    }
}