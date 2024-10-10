package com.atguigu.daijia.map.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.map.service.MapService;
import com.atguigu.daijia.model.form.map.CalculateDrivingLineForm;
import com.atguigu.daijia.model.vo.map.DrivingLineVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class MapServiceImpl implements MapService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${tencent.map.key}")
    private String key;

    /**
     * 参考腾讯文档
     * https://lbs.qq.com/service/webService/webServiceGuide/route/webServiceRoute
     */
    @Override
    public DrivingLineVo calculateDrivingLine(CalculateDrivingLineForm calculateDrivingLineForm) {

        String url = "https://apis.map.qq.com/ws/direction/v1/driving/?from={}&to={}&key={key}";
        // 1.拼接调用参数
        HashMap<String, String> map = new HashMap<>();
        //  起、终点位置坐标 格式：纬度在前，经度在后，半角逗号分隔
        //  from=40.034852,116.319820  to=39.771075,116.351395
        String from = calculateDrivingLineForm.getStartPointLatitude() + "," + calculateDrivingLineForm.getStartPointLongitude();
        String to = calculateDrivingLineForm.getEndPointLatitude() + "," + calculateDrivingLineForm.getEndPointLongitude();
        // 开始位置
        map.put("from", from);
        // 结束位置
        map.put("to", to);
        // key
        map.put("key", key);

        // 2.调用腾讯接口
        JSONObject result = restTemplate.getForObject(url, JSONObject.class, map);
        if (result.getIntValue("status") != 0) {
            log.error("地图接口出错，调用状态说明：{}", result.getString("message"));
            log.error("接口返回数据：{}", JSONObject.toJSONString(result));
            throw new GuiguException(ResultCodeEnum.MAP_FAIL);
        }
        //返回第一条最佳线路
        JSONObject route = result.getJSONObject("result").getJSONArray("routes").getJSONObject(0);
        DrivingLineVo drivingLineVo = new DrivingLineVo();
        //单位：千米 distance/1000（保留两位小数+四舍五入)
        drivingLineVo.setDistance(route.getBigDecimal("distance").divide(new BigDecimal(1000)).setScale(2, RoundingMode.HALF_UP));
        // 时间
        drivingLineVo.setDuration(route.getBigDecimal("duration"));
        // 路线
        drivingLineVo.setPolyline(route.getJSONArray("polyline"));
        return drivingLineVo;
    }
}
