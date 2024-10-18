package com.atguigu.daijia.dispatch.xxl.job;

import com.atguigu.daijia.dispatch.mapper.XxlJobLogMapper;
import com.atguigu.daijia.dispatch.service.NewOrderService;
import com.atguigu.daijia.model.entity.dispatch.XxlJobLog;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobHandler {

    @Autowired
    private XxlJobLogMapper xxlJobLogMapper;

    @Autowired
    private NewOrderService newOrderService;

    @XxlJob("newOrderTaskHandler")
    public void newOrderTaskHandler() {
        // 记录任务日志调用信息
        XxlJobLog xxlJobLog = new XxlJobLog();
        long jobId = XxlJobHelper.getJobId();
        xxlJobLog.setJobId(jobId);
        long startTime = System.currentTimeMillis();
        try {
            //执行任务：搜索附近司机
            newOrderService.executeTask(jobId);
            //成功状态
            xxlJobLog.setStatus(1);
        } catch (Exception e) {
            String message = e.getMessage();
            xxlJobLog.setError(message);
            xxlJobLog.setStatus(0);
        } finally {
            long times = System.currentTimeMillis()-startTime;
            xxlJobLog.setTimes(times);
            xxlJobLogMapper.insert(xxlJobLog);
        }

    }
}
