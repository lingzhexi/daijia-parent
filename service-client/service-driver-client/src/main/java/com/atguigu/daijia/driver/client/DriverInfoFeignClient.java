package com.atguigu.daijia.driver.client;

import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.model.form.driver.UpdateDriverAuthInfoForm;
import com.atguigu.daijia.model.vo.driver.DriverAuthInfoVo;
import com.atguigu.daijia.model.vo.driver.DriverLoginVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(value = "service-driver")
public interface DriverInfoFeignClient {

    @GetMapping("/driver/info/login/{code}")
    public Result<Long> login(@PathVariable String code);

    @GetMapping("/driver/info/getDriverLoginInfo/{driverId}")
    public Result<DriverLoginVo> getDriverLoginInfo(@PathVariable Long driverId);

    @GetMapping("/driver/info/getDriverAuthInfo/{driverId}")
    public Result<DriverAuthInfoVo> getDriverAuthInfo(@PathVariable("driverId") Long driverId);

    @PostMapping("/diver/info/updateDriverAuthInfo")
    public Result<Boolean> updateDriverAuthInfo(@RequestBody UpdateDriverAuthInfoForm updateDriverAuthInfoForm);
}