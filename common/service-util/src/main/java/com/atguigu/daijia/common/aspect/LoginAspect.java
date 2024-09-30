package com.atguigu.daijia.common.aspect;

import com.atguigu.daijia.common.annotation.Login;
import com.atguigu.daijia.common.constant.RedisConstant;
import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.common.util.AuthContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 操作用户登陆处理
 */
@Component
@Aspect  //切面类
public class LoginAspect {
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private RedisTemplate redisTemplate;

    //环绕通知,登录判断
    //切入点表达式：指定对那些规则的方法进行增强
    //  execution(* 匹配路径)
    //        具体来说，它表示匹配com.atguigu.daijia包及其子包下的任何类（.*）中的
    //        controller包中的任何类（再次.*）中的任何方法（最后的.*），方法可以接受任意数量和类型的参数（(..)）。
    //        @annotation
    // @Around 中表示：在规定的方法中 && 被 @Login 注解 生效
    //        Login login：这个参数是一个注解对象，当方法被标注了@Login注解时，这里可以获取到这个注解的实例，
    //        从而可以访问注解的属性等信息。
    // ProceedingJoinPoint 表示切点
    @Around("execution(* com.atguigu.daijia.*.controller.*.*(..))&& @annotation(login)")
    public Object aroundLogin(ProceedingJoinPoint proceedingJoinPoint, Login login) throws Throwable{
        //1.获取request对象
//        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
//        ServletRequestAttributes sra = (ServletRequestAttributes) requestAttributes;
//        HttpServletRequest request = sra.getRequest();

        //2.从请求头获取token
        String token = request.getHeader("token");

        //3.判断token是否为空，为空,返回登录提示
        if (!StringUtils.hasText(token)) {
            throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
        }

        //4.token非空，查询redis
        String customerId = (String) redisTemplate.opsForValue().get(RedisConstant.USER_LOGIN_KEY_PREFIX + token);

        //5.查询redis对应用户 id，把用户id放入ThreadLocal里面（当前线程绑定对象）
        if (StringUtils.hasText(customerId)) {
            AuthContextHolder.setUserId(Long.parseLong(customerId));
        }
        //6.执行业务方法
       return proceedingJoinPoint.proceed();
    }
}
