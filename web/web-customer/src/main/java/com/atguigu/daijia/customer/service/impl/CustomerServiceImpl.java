package com.atguigu.daijia.customer.service.impl;

import com.atguigu.daijia.common.constant.RedisConstant;
import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.customer.client.CustomerInfoFeignClient;
import com.atguigu.daijia.customer.service.CustomerService;
import com.atguigu.daijia.model.form.customer.UpdateWxPhoneForm;
import com.atguigu.daijia.model.vo.customer.CustomerLoginVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class CustomerServiceImpl implements CustomerService {

    //注入远程调用接口
    @Autowired
    private CustomerInfoFeignClient client;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public String login(String code) {
        // 1.拿code 远程调用 换openid
        Result<Long> loginResult = client.login(code);

        // 2.判断返回失败，提示错误
        Integer codeResult = loginResult.getCode();
        if (codeResult != 200) {
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        // 3.获取远程调用返回用户Id
        Long customerId = loginResult.getData();

        // 4.判断返回用户 Id 是否为空，为空，提示错误
        if (customerId == null) {
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }

        // 5.生成token字符串
        String token = UUID.randomUUID().toString().replaceAll("-", "");

        // 6.把用户Id放到Redis，设置过期时间
        // key:token value:customerId
        // redisTemplate.opsForValue().set(token,customerId.toString(),30,TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(RedisConstant.USER_LOGIN_KEY_PREFIX + token,
                customerId.toString(),
                RedisConstant.USER_LOGIN_KEY_TIMEOUT, TimeUnit.SECONDS);

        // 7.返回token
        return token;
    }

    /**
     * 获取用户信息
     *
     * @param token
     * @return
     */
    @Override
    public CustomerLoginVo getCustomerLoginInfo(String token) {
        // 2.根据token查询redis 查询出token在redis 里面对应的用户id
        String customerId = (String) redisTemplate.opsForValue().get(RedisConstant.USER_LOGIN_KEY_PREFIX + token);
        if (!StringUtils.hasText(customerId)) {
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }

        // 3.根据用户id进行远程调用，得到用户信息
        Result<CustomerLoginVo> result = client.getCustomerLoginInfo(Long.parseLong(customerId));
        if (result.getCode() != 200) {
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        CustomerLoginVo customerLoginVo = result.getData();
        if (customerLoginVo == null) {
            throw new GuiguException(ResultCodeEnum.SERVICE_ERROR);
        }
        // 4.返回用户登录信息
        return customerLoginVo;
    }

    @Override
    public CustomerLoginVo getCustomerInfo(Long customerId) {
        // 1.根据用户id进行远程调用，得到用户信息
        Result<CustomerLoginVo> result = client.getCustomerLoginInfo(customerId);
        if (result.getCode() != 200) {
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }

        CustomerLoginVo customerLoginVo = result.getData();
        if (customerLoginVo == null) {
            throw new GuiguException(ResultCodeEnum.SERVICE_ERROR);
        }
        // 2.返回用户登录信息
        return customerLoginVo;
    }

}
