package com.atguigu.daijia.order.service.impl;

import com.atguigu.daijia.model.entity.order.OrderInfo;
import com.atguigu.daijia.model.entity.order.OrderStatusLog;
import com.atguigu.daijia.model.enums.OrderStatus;
import com.atguigu.daijia.model.form.order.OrderInfoForm;
import com.atguigu.daijia.order.mapper.OrderInfoMapper;
import com.atguigu.daijia.order.mapper.OrderStatusLogMapper;
import com.atguigu.daijia.order.service.OrderInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Autowired
    private OrderInfoMapper infoMapper;

    @Autowired
    private OrderStatusLogMapper statusLogMapper;

    /**
     * 更新订单信息
     * @param orderInfoForm
     * @return
     */
    @Override
    public Long saveOrderInfo(OrderInfoForm orderInfoForm) {
        // 拼接OrderInfo
        OrderInfo orderInfo = new OrderInfo();
        BeanUtils.copyProperties(orderInfoForm, orderInfo);

        // 订单号
        String orderNo = UUID.randomUUID().toString().replace("-", "");
        orderInfo.setOrderNo(orderNo);
        // 订单状态
        orderInfo.setStatus(OrderStatus.WAITING_ACCEPT.getStatus());

        // 保存OrderInfo
        infoMapper.insert(orderInfo);
        // 记录订单状态日志信息
        this.saveStatusLog(orderInfo.getId(),orderInfo.getStatus());
        return orderInfo.getId();
    }

    public void saveStatusLog(Long orderId,Integer status) {
        OrderStatusLog orderStatusLog = new OrderStatusLog();
        orderStatusLog.setOrderId(orderId);
        orderStatusLog.setOrderStatus(status);
        orderStatusLog.setOperateTime(new Date());
        statusLogMapper.insert(orderStatusLog);
    }
}
