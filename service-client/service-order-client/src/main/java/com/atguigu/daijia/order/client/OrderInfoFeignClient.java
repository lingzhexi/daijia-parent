package com.atguigu.daijia.order.client;

import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.model.form.order.OrderInfoForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(value = "service-order")
public interface OrderInfoFeignClient {

    /**
     * 保存订单信息
     *
     * @param orderInfoForm
     * @return
     */
    @PostMapping("/order/info/saveOrderInfo")
    Result<Long> saveOrderInfo(@RequestBody OrderInfoForm orderInfoForm);

}