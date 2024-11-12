package com.atguigu.daijia.driver.service;

import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.model.vo.order.NewOrderDataVo;

import java.util.List;

public interface OrderService {

    List<NewOrderDataVo> findNewOrderQueueData(Long driverId);

    Boolean clearNewOrderQueueData(Long driverId);

    Boolean robNewOrder(Long driverId, Long orderId);

}
