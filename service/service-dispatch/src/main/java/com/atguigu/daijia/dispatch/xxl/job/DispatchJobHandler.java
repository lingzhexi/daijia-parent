package com.atguigu.daijia.dispatch.xxl.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

@Component
public class DispatchJobHandler {
    @XxlJob("firstJobHandler")
    public void testJobHandler() {
        System.out.println("xxl-job集成测试");

    }
}
