package com.atguigu.daijia.dispatch.client;

import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.model.vo.dispatch.NewOrderTaskVo;
import com.atguigu.daijia.model.vo.order.NewOrderDataVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


@FeignClient(value = "service-dispatch")
public interface NewOrderFeignClient {

    /**
     * 添加并启动新订单调度
     *
     * @param newOrderTaskVo
     * @return
     */
    @PostMapping("/dispatch/newOrder/addAndStartTask")
    Result<Long> addAndStartTask(@RequestBody NewOrderTaskVo newOrderTaskVo);

    /**
     * 查询司机新订单数据
     *
     * @param driverId
     * @return
     */
    @PostMapping("/dispatch/newOrder/findNewOrderQueueData/{driverId}")
    Result<List<NewOrderDataVo>> findNewOrderQueueData(@PathVariable Long driverId);

    /**
     * 清空司机新订单队列
     *
     * @param driverId
     * @return
     */
    @GetMapping("/dispatch/newOrder/clearNewOrderQueueData/{driverId}")
    Result<Boolean> clearNewOrderQueueData(@PathVariable Long driverId);

}
