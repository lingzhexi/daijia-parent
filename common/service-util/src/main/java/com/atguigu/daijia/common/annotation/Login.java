package com.atguigu.daijia.common.annotation;

import java.lang.annotation.*;

/**
 * 登录判断
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Login {

}
