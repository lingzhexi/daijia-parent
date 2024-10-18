package com.atguigu.daijia.dispatch.xxl.client;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.dispatch.xxl.config.XxlJobClientConfig;
import com.atguigu.daijia.model.entity.dispatch.XxlJobInfo;
import com.xxl.job.core.glue.GlueTypeEnum;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * https://dandelioncloud.cn/article/details/1598865461087518722
 */
@Slf4j
@Component
public class XxlJobClient {

    @Autowired
    private XxlJobClientConfig xxlJobClientConfig;

    //客户端调用服务端里面的方法
    @Autowired
    private RestTemplate restTemplate;

    /**
     * 新增任务
     * @param executorHandler
     * @param param
     * @param corn
     * @param desc
     * @return
     */
    @SneakyThrows
    public Long addJob(String executorHandler, String param, String corn, String desc){
        //生成xxlJobInfo对象
        XxlJobInfo xxlJobInfo = getAddJobInfo(executorHandler, param, corn, desc);

        //生成 HttpEntity对象
        HttpEntity<XxlJobInfo> request = getHttpEntity(xxlJobInfo);

        //调用接口
        String url = xxlJobClientConfig.getAddUrl();
        ResponseEntity<JSONObject> response = restTemplate.postForEntity(url, request, JSONObject.class);
        if(response.getStatusCode().value() == 200 && response.getBody().getIntValue("code") == 200) {
            log.info("增加xxl执行任务成功,返回信息:{}", response.getBody().toJSONString());
            //content为任务id
            return response.getBody().getLong("content");
        }
        log.info("调用xxl增加执行任务失败:{}", response.getBody().toJSONString());
        throw new GuiguException(ResultCodeEnum.XXL_JOB_ERROR);
    }



    /**
     * 启动任务
     * @param jobId
     * @return
     */
    public Boolean startJob(Long jobId) {
        XxlJobInfo xxlJobInfo = new XxlJobInfo();
        xxlJobInfo.setId(jobId.intValue());

        //生成 HttpEntity对象
        HttpEntity<XxlJobInfo> request = getHttpEntity(xxlJobInfo);

        //调用接口
        String url = xxlJobClientConfig.getStartJobUrl();
        ResponseEntity<JSONObject> response = restTemplate.postForEntity(url, request, JSONObject.class);
        if(response.getStatusCode().value() == 200 && response.getBody().getIntValue("code") == 200) {
            log.info("启动xxl执行任务成功:{},返回信息:{}", jobId, response.getBody().toJSONString());
            return true;
        }
        log.info("启动xxl执行任务失败:{},返回信息:{}", jobId, response.getBody().toJSONString());
        throw new GuiguException(ResultCodeEnum.XXL_JOB_ERROR);
    }

    /**
     * 停止任务
     * @param jobId 任务Id
     * @return
     */
    public Boolean stopJob(Long jobId) {
        XxlJobInfo xxlJobInfo = new XxlJobInfo();
        xxlJobInfo.setId(jobId.intValue());

        //生成 HttpEntity对象
        HttpEntity<XxlJobInfo> request = getHttpEntity(xxlJobInfo);

        //调用接口
        String url = xxlJobClientConfig.getStopJobUrl();
        ResponseEntity<JSONObject> response = restTemplate.postForEntity(url, request, JSONObject.class);
        if(response.getStatusCode().value() == 200 && response.getBody().getIntValue("code") == 200) {
            log.info("停止xxl执行任务成功:{},返回信息:{}", jobId, response.getBody().toJSONString());
            return true;
        }
        log.info("停止xxl执行任务失败:{},返回信息:{}", jobId, response.getBody().toJSONString());
        throw new GuiguException(ResultCodeEnum.XXL_JOB_ERROR);
    }

    /**
     * 删除任务
     * @param jobId 任务Id
     * @return
     */
    public Boolean removeJob(Long jobId) {
        XxlJobInfo xxlJobInfo = new XxlJobInfo();
        xxlJobInfo.setId(jobId.intValue());

        //生成 HttpEntity对象
        HttpEntity<XxlJobInfo> request = getHttpEntity(xxlJobInfo);

        //调用接口
        String url = xxlJobClientConfig.getRemoveUrl();
        ResponseEntity<JSONObject> response = restTemplate.postForEntity(url, request, JSONObject.class);
        if(response.getStatusCode().value() == 200 && response.getBody().getIntValue("code") == 200) {
            log.info("删除xxl执行任务成功:{},返回信息:{}", jobId, response.getBody().toJSONString());
            return true;
        }
        log.info("删除xxl执行任务失败:{},返回信息:{}", jobId, response.getBody().toJSONString());
        throw new GuiguException(ResultCodeEnum.XXL_JOB_ERROR);
    }



    /**
     * 新增并启动任务
     * @param executorHandler
     * @param param
     * @param corn
     * @param desc
     * @return
     */
    public Long addAndStart(String executorHandler, String param, String corn, String desc) {
        //生成xxlJobInfo对象
        XxlJobInfo xxlJobInfo = getAddJobInfo(executorHandler, param, corn, desc);

        //生成 HttpEntity对象
        HttpEntity<XxlJobInfo> request = getHttpEntity(xxlJobInfo);

        //调用接口
        String url = xxlJobClientConfig.getAddAndStartUrl();
        ResponseEntity<JSONObject> response = restTemplate.postForEntity(url, request, JSONObject.class);
        if(response.getStatusCode().value() == 200 && response.getBody().getIntValue("code") == 200) {
            log.info("增加并开始执行xxl任务成功,返回信息:{}", response.getBody().toJSONString());
            //content为任务id
            return response.getBody().getLong("content");
        }
        log.info("增加并开始执行xxl任务失败:{}", response.getBody().toJSONString());
        throw new GuiguException(ResultCodeEnum.XXL_JOB_ERROR);
    }

    /**
     *  生成xxlJobInfo对象
     * @param executorHandler
     * @param param
     * @param corn
     * @param desc
     * @return
     */
    private XxlJobInfo getAddJobInfo(String executorHandler, String param, String corn, String desc) {
        XxlJobInfo xxlJobInfo = new XxlJobInfo();
        xxlJobInfo.setJobGroup(xxlJobClientConfig.getJobGroupId());
        xxlJobInfo.setJobDesc(desc);
        xxlJobInfo.setAuthor("lingzx");
        xxlJobInfo.setScheduleType("CRON");
        xxlJobInfo.setScheduleConf(corn);
        xxlJobInfo.setGlueType(GlueTypeEnum.BEAN.getDesc());
        xxlJobInfo.setExecutorHandler(executorHandler);
        xxlJobInfo.setExecutorParam(param);
        xxlJobInfo.setExecutorRouteStrategy("FIRST");
        xxlJobInfo.setExecutorBlockStrategy("SERIAL_EXECUTION");
        xxlJobInfo.setMisfireStrategy("FIRE_ONCE_NOW");
        xxlJobInfo.setExecutorTimeout(0);
        xxlJobInfo.setExecutorFailRetryCount(0);
        return xxlJobInfo;
    }

    /**
     * 生成HttpEntity对象
     * @param xxlJobInfo
     * @return
     */
    private HttpEntity<XxlJobInfo> getHttpEntity(XxlJobInfo xxlJobInfo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<XxlJobInfo> request = new HttpEntity<>(xxlJobInfo, headers);
        return request;
    }

}