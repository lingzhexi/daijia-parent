package com.atguigu.daijia.driver.controller;

import com.atguigu.daijia.common.annotation.Login;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.common.util.AuthContextHolder;
import com.atguigu.daijia.dispatch.client.NewOrderFeignClient;
import com.atguigu.daijia.model.vo.order.CurrentOrderInfoVo;
import com.atguigu.daijia.model.vo.order.NewOrderDataVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Tag(name = "订单API接口管理")
@RestController
@RequestMapping("/order")
@SuppressWarnings({"unchecked", "rawtypes"})
public class OrderController {

    @Autowired
    private NewOrderFeignClient newOrderFeignClient;

    /**
     * 清空司机新订单数据
     *
     * @return
     */
    @Operation(summary = "查询司机新订单数据")
    @Login
    @PostMapping("/findNewOrderQueueData")
    public Result<List<NewOrderDataVo>> findNewOrderQueueData() {
        Long driverId = AuthContextHolder.getUserId();
        return newOrderFeignClient.findNewOrderQueueData(driverId);
    }

    /**
     * 清空司机新订单数据
     *
     * @return
     */
    @Operation(summary = "清空司机新订单数据")
    @Login
    @GetMapping("/clearNewOrderQueueData")
    public Result<Boolean> clearNewOrderQueueData() {
        Long driverId = AuthContextHolder.getUserId();
        return newOrderFeignClient.clearNewOrderQueueData(driverId);
    }

    @Operation(summary = "搜索当前订单")
    @Login
    @GetMapping("/searchDriverCurrentOrder")
    public Result<CurrentOrderInfoVo> searchDriverCurrentOrder() {
        CurrentOrderInfoVo currentOrderInfoVo = new CurrentOrderInfoVo();
        currentOrderInfoVo.setIsHasCurrentOrder(false);
        return Result.ok(currentOrderInfoVo);
    }
}

