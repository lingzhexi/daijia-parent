package com.atguigu.daijia.customer.service;

import com.atguigu.daijia.model.vo.customer.CustomerInfoVo;
import com.atguigu.daijia.model.vo.customer.CustomerLoginVo;

public interface CustomerService {

    //微信登录 返回token
    String login(String code);

    //获取用户信息
    CustomerLoginVo getCustomerLoginInfo(String token);
}
