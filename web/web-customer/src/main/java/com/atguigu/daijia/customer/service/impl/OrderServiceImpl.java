package com.atguigu.daijia.customer.service.impl;

import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.customer.service.OrderService;
import com.atguigu.daijia.map.client.MapFeignClient;
import com.atguigu.daijia.model.form.customer.ExpectOrderForm;
import com.atguigu.daijia.model.form.customer.SubmitOrderForm;
import com.atguigu.daijia.model.form.map.CalculateDrivingLineForm;
import com.atguigu.daijia.model.form.order.OrderInfoForm;
import com.atguigu.daijia.model.form.rules.FeeRuleRequestForm;
import com.atguigu.daijia.model.vo.customer.ExpectOrderVo;
import com.atguigu.daijia.model.vo.map.DrivingLineVo;
import com.atguigu.daijia.model.vo.rules.FeeRuleResponseVo;
import com.atguigu.daijia.order.client.OrderInfoFeignClient;
import com.atguigu.daijia.rules.client.FeeRuleFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class OrderServiceImpl implements OrderService {

    @Autowired
    private MapFeignClient mapFeignClient;

    @Autowired
    private FeeRuleFeignClient feeRuleFeignClient;

    @Autowired
    private OrderInfoFeignClient orderInfoFeignClient;

    //预估订单数据
    @Override
    public ExpectOrderVo expectOrderVo(ExpectOrderForm expectOrderForm) {
        //获取驾驶线路数据
        CalculateDrivingLineForm calculateDrivingLineForm = new CalculateDrivingLineForm();
        BeanUtils.copyProperties(expectOrderForm,calculateDrivingLineForm);
        Result<DrivingLineVo> drivingLineVoResult = mapFeignClient.calculateDrivingLine(calculateDrivingLineForm);
        DrivingLineVo drivingLine = drivingLineVoResult.getData();

        //拼接订单规则数据
        FeeRuleRequestForm feeRuleRequestForm = new FeeRuleRequestForm();
        feeRuleRequestForm.setDistance(drivingLine.getDistance());
        feeRuleRequestForm.setStartTime(new Date());
        feeRuleRequestForm.setWaitMinute(0);

        //获取预估订单结果
        Result<FeeRuleResponseVo> feeRuleResponseVoResult = feeRuleFeignClient.calculateOrderFee(feeRuleRequestForm);

        //拼接返回数据
        ExpectOrderVo expectOrderVo = new ExpectOrderVo();
        expectOrderVo.setDrivingLineVo(drivingLine);
        expectOrderVo.setFeeRuleResponseVo(feeRuleResponseVoResult.getData());
        return expectOrderVo;
    }

    // 乘客下单
    @Override
    public Long submitOrder(SubmitOrderForm submitOrderForm) {
        //获取预估费用
        CalculateDrivingLineForm lineForm = new CalculateDrivingLineForm();
        BeanUtils.copyProperties(submitOrderForm, lineForm);
        Result<DrivingLineVo> drivingLineVoResult = mapFeignClient.calculateDrivingLine(lineForm);
        DrivingLineVo drivingLine = drivingLineVoResult.getData();

        //获取预估里程
        FeeRuleRequestForm feeRuleRequestForm = new FeeRuleRequestForm();
        feeRuleRequestForm.setDistance(drivingLine.getDistance());
        feeRuleRequestForm.setStartTime(new Date());
        feeRuleRequestForm.setWaitMinute(0);
        Result<FeeRuleResponseVo> feeRuleResponseVoResult = feeRuleFeignClient.calculateOrderFee(feeRuleRequestForm);
        FeeRuleResponseVo feeRuleData = feeRuleResponseVoResult.getData();

        //保存订单信息
        OrderInfoForm orderInfoForm = new OrderInfoForm();
        BeanUtils.copyProperties(submitOrderForm, orderInfoForm);
        orderInfoForm.setExpectAmount(feeRuleData.getTotalAmount());
        orderInfoForm.setExpectDistance(drivingLine.getDistance());
        Result<Long> orderInfoResult = orderInfoFeignClient.saveOrderInfo(orderInfoForm);

        //TODO 查询附近可以接单的司机
        return orderInfoResult.getData();
    }
}
