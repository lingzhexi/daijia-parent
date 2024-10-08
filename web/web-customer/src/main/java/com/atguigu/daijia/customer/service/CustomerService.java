package com.atguigu.daijia.customer.service;

import com.atguigu.daijia.model.form.customer.UpdateWxPhoneForm;
import com.atguigu.daijia.model.vo.customer.CustomerLoginVo;

public interface CustomerService {

    //微信登录 返回token
    String login(String code);

    //获取用户信息
    CustomerLoginVo getCustomerLoginInfo(String token);

    //获取用户信息
    CustomerLoginVo getCustomerInfo(Long customerId);

    //获取微信用户手机号
    Boolean updateWxPhoneNumber(UpdateWxPhoneForm updateWxPhoneForm);
}
