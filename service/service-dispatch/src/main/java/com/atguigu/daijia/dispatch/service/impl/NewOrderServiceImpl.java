package com.atguigu.daijia.dispatch.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.atguigu.daijia.common.constant.RedisConstant;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.dispatch.mapper.OrderJobMapper;
import com.atguigu.daijia.dispatch.service.NewOrderService;
import com.atguigu.daijia.dispatch.xxl.client.XxlJobClient;
import com.atguigu.daijia.dispatch.xxl.config.XxlJobClientConfig;
import com.atguigu.daijia.map.client.LocationFeignClient;
import com.atguigu.daijia.model.entity.dispatch.OrderJob;
import com.atguigu.daijia.model.enums.OrderStatusEnum;
import com.atguigu.daijia.model.form.map.SearchNearByDriverForm;
import com.atguigu.daijia.model.vo.dispatch.NewOrderTaskVo;
import com.atguigu.daijia.model.vo.map.NearByDriverVo;
import com.atguigu.daijia.model.vo.order.NewOrderDataVo;
import com.atguigu.daijia.order.client.OrderInfoFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author geekcode
 */
@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class NewOrderServiceImpl implements NewOrderService {

    @Autowired
    private XxlJobClient xxlJobClient;

    @Autowired
    private XxlJobClientConfig xxlJobClientConfig;

    @Autowired
    private OrderJobMapper orderJobMapper;

    @Autowired
    private OrderInfoFeignClient orderInfoFeignClient;

    @Autowired
    private LocationFeignClient locationFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    //添加开始新订单任务
    @Override
    public Long addAndStartTask(NewOrderTaskVo newOrderTaskVo) {
        //1 判断订单是否启动项目
        LambdaQueryWrapper<OrderJob> query = new LambdaQueryWrapper<>();
        query.eq(OrderJob::getOrderId, newOrderTaskVo.getOrderId());
        OrderJob orderJob = orderJobMapper.selectOne(query);
        if (orderJob != null) {
            return orderJob.getJobId();
        }
        //2 没有启动 进行操作
        //   调用任务 每分钟执行一次
        String executorHandler = "newOrderTaskHandler";
        String desc = "添加新订单任务调度：" + newOrderTaskVo.getOrderId();
        String corn = xxlJobClientConfig.getAddOrderCorn();
        Long jobId = xxlJobClient.addAndStart(executorHandler, "", corn, desc);

        //记录任务调度信息 添加orderId、jobId
        orderJob.setJobId(jobId);
        orderJob.setOrderId(newOrderTaskVo.getOrderId());
        orderJob.setParameter(JSONObject.toJSONString(newOrderTaskVo));
        orderJobMapper.insert(orderJob);
        return orderJob.getJobId();
    }

    /**
     * 搜索附近司机
     *
     * @param jobId
     */
    @Override
    public void executeTask(long jobId) {
        //1根据jobId查询数据库，当前任务是否已经创建，//如果没有创建，不往下执行了
        LambdaQueryWrapper<OrderJob> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderJob::getJobId, queryWrapper);
        OrderJob orderJob = orderJobMapper.selectOne(queryWrapper);
        if (orderJob == null) {
            return;
        }
        //2 查询订单状态，如果当前订单接单状态，继续执行。如果当前订单不是接单状态，停止任务调度
        Result<Integer> orderStatusResult = orderInfoFeignClient.getOrderStatus(orderJob.getOrderId());
        Integer orderStatus = orderStatusResult.getData();
        if (orderStatus.intValue() != OrderStatusEnum.WAITING_ACCEPT.getStatus().intValue()) {
            //停止任务调度
            xxlJobClient.stopJob(jobId);
            return;
        }
        //3远程调用：搜索附近满足条件可以接单司机
        //4远程调用之后，获取满足可以接单司机集合
        NewOrderTaskVo orderTask = JSONObject.parseObject(orderJob.getParameter(), NewOrderTaskVo.class);
        SearchNearByDriverForm searchNearByDriverForm = new SearchNearByDriverForm();
        searchNearByDriverForm.setLatitude(orderTask.getEndPointLatitude());
        searchNearByDriverForm.setLongitude(orderTask.getEndPointLongitude());
        searchNearByDriverForm.setMileageDistance(orderTask.getExpectDistance());
        //远程调用
        Result<List<NearByDriverVo>> listResult = locationFeignClient.searchNearByDriver(searchNearByDriverForm);
        List<NearByDriverVo> driverList = listResult.getData();
        //5遍历司机集合，得到每个司机，为每个司机创建临时队列，存储新订单信息
        driverList.forEach(driver -> {
            //根据Id生成key
            String repeatKey = RedisConstant.DRIVER_ORDER_REPEAT_LIST + orderTask.getOrderId();
            // 防止重推
            Boolean isMember = redisTemplate.opsForSet().isMember(repeatKey, driver.getDriverId());
            if (!isMember) {
                //消息推送给司机（15分钟过期）
                redisTemplate.opsForSet().add(repeatKey, driver.getDriverId());
                redisTemplate.expire(repeatKey, RedisConstant.DRIVER_ORDER_REPEAT_LIST_EXPIRES_TIME, TimeUnit.MINUTES);
                //司机接收多条数据（1分钟过期）
                NewOrderDataVo newOrderDataVo = new NewOrderDataVo();
                newOrderDataVo.setOrderId(orderTask.getOrderId());
                newOrderDataVo.setStartLocation(orderTask.getStartLocation());
                newOrderDataVo.setEndLocation(orderTask.getEndLocation());
                newOrderDataVo.setExpectAmount(orderTask.getExpectAmount());
                newOrderDataVo.setExpectDistance(orderTask.getExpectDistance());
                newOrderDataVo.setExpectTime(orderTask.getExpectTime());
                newOrderDataVo.setFavourFee(orderTask.getFavourFee());
                newOrderDataVo.setDistance(driver.getDistance());
                newOrderDataVo.setCreateTime(orderTask.getCreateTime());
                //订单加入司机临时队列
                String key = RedisConstant.DRIVER_ORDER_TEMP_LIST + driver.getDriverId();
                redisTemplate.opsForList().leftPush(key, JSONObject.toJSONString(newOrderDataVo));
                redisTemplate.expire(key, RedisConstant.DRIVER_ORDER_TEMP_LIST_EXPIRES_TIME, TimeUnit.MINUTES);
            }
        });

    }

    @Override
    public List<NewOrderDataVo> findNewOrderQueueData(Long driverId) {
        //从临时队列中取出最新的数据order
        List<NewOrderDataVo> list = new ArrayList<>();
        String key = RedisConstant.DRIVER_ORDER_TEMP_LIST + driverId;
        Long size = redisTemplate.opsForList().size(key);
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                String content = (String) redisTemplate.opsForList().leftPop(key);
                NewOrderDataVo newOrderDataVo = JSONObject.parseObject(content, NewOrderDataVo.class);
                list.add(newOrderDataVo);
            }
        }
        return list;
    }

    @Override
    public Boolean clearNewOrderQueueData(Long driverId) {
        String key = RedisConstant.DRIVER_ORDER_TEMP_LIST + driverId;
        redisTemplate.delete(key);
        return true;
    }
}
