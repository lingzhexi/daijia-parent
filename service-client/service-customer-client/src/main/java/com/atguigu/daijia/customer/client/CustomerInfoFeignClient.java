package com.atguigu.daijia.customer.client;

import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.model.form.customer.UpdateWxPhoneForm;
import com.atguigu.daijia.model.vo.customer.CustomerLoginVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "service-customer")
public interface CustomerInfoFeignClient {
    /**
     *  授权微信登录
     * @param code
     * @return 用户Id
     */
    @GetMapping("/customer/info/login/{code}")
    public Result<Long> login(@PathVariable String code);

    /**
     * 获取用户登录信息
     *
     * @param customerId
     * @return 用户登录信息
     */
    @GetMapping("/customer/info/getCustomerLoginInfo/{customerId}")
    public Result<CustomerLoginVo> getCustomerLoginInfo(@PathVariable("customerId") Long customerId);

    /**
     * 获取微信用户手机号
     * @param updateWxPhoneForm
     * @return
     */
    @PostMapping("/customer/info/updateWxPhoneNumber")
    public Boolean updateWxPhoneNumber(@RequestBody UpdateWxPhoneForm updateWxPhoneForm);
}