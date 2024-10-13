package com.atguigu.daijia.driver.controller;

import com.atguigu.daijia.common.annotation.Login;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.common.util.AuthContextHolder;
import com.atguigu.daijia.driver.service.LocationService;
import com.atguigu.daijia.model.form.map.UpdateDriverLocationForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "位置API接口管理")
@RestController
@RequestMapping(value="/location")
@SuppressWarnings({"unchecked", "rawtypes"})
public class LocationController {
    @Autowired
    private LocationService locationService;

    @Operation(summary = "开启接单模式:更新司机经纬度位置")
    @Login
    @PostMapping("/updateDriverLocation")
    public Result<Boolean> updateDriverLocation(@RequestBody UpdateDriverLocationForm updateDriverLocationForm) {
        updateDriverLocationForm.setDriverId(AuthContextHolder.getUserId());
        return Result.ok(locationService.updateDriverLocation(updateDriverLocationForm));
    }

}

