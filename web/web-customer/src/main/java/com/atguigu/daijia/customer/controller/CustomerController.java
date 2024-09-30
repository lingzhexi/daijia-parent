package com.atguigu.daijia.customer.controller;

import com.atguigu.daijia.common.annotation.Login;
import com.atguigu.daijia.common.constant.RedisConstant;
import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.common.util.AuthContextHolder;
import com.atguigu.daijia.customer.service.CustomerService;
import com.atguigu.daijia.model.form.customer.UpdateWxPhoneForm;
import com.atguigu.daijia.model.vo.customer.CustomerInfoVo;
import com.atguigu.daijia.model.vo.customer.CustomerLoginVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "客户API接口管理")
@RestController
@RequestMapping("/customer")
@SuppressWarnings({"unchecked", "rawtypes"})
public class CustomerController {

    @Autowired
    private CustomerService customerInfoService;
/*
    @Operation(summary = "获取用户登录信息")
    @GetMapping("/getCustomerLoginInfo")
    public Result<CustomerLoginVo> getCustomerLoginInfo(@RequestHeader(value = "token") String token) {
        // 1、获取请求头中的 token 字符串
        // HttpServletRequest request
        // String token = request.getHeader("token");
        return Result.ok(customerInfoService.getCustomerLoginInfo(token));
    }
    //新增：使用自定注解+aop处理，获取请求头的token
*/
    /**
     * 获取用户登录信息，使用自定义注解 @Login +切面获取token
     * @return
     */
    @Login
    @Operation(summary = "获取用户登录信息")
    @GetMapping("/getCustomerLoginInfo")
    public Result<CustomerLoginVo> getCustomerLoginInfo() {
        // 1、从ThreadLoad获取 token 字符串
        Long customerId = AuthContextHolder.getUserId();
        // 2、调用service
        return Result.ok(customerInfoService.getCustomerInfo(customerId));
    }

    /**
     * 授权登陆 code 换取 token
     *
     * @param code
     * @return
     */
    @Operation(summary = "小程序授权登录")
    @GetMapping("/login/{code}")
    public Result<String> wxLogin(@PathVariable String code) {
        return Result.ok(customerInfoService.login(code));
    }



}