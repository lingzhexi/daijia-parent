package com.atguigu.daijia.driver.client;

import com.atguigu.daijia.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-driver")
public interface DriverInfoFeignClient {

    @GetMapping("/driver/info/login/{code}")
    public Result<Long> login(@PathVariable String code);

}