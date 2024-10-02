package com.atguigu.daijia.driver.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.atguigu.daijia.common.constant.SystemConstant;
import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.driver.mapper.DriverAccountMapper;
import com.atguigu.daijia.driver.mapper.DriverInfoMapper;
import com.atguigu.daijia.driver.mapper.DriverLoginLogMapper;
import com.atguigu.daijia.driver.mapper.DriverSetMapper;
import com.atguigu.daijia.driver.service.CosService;
import com.atguigu.daijia.driver.service.DriverInfoService;
import com.atguigu.daijia.model.entity.driver.DriverAccount;
import com.atguigu.daijia.model.entity.driver.DriverInfo;
import com.atguigu.daijia.model.entity.driver.DriverLoginLog;
import com.atguigu.daijia.model.entity.driver.DriverSet;
import com.atguigu.daijia.model.vo.driver.DriverAuthInfoVo;
import com.atguigu.daijia.model.vo.driver.DriverLoginVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class DriverInfoServiceImpl extends ServiceImpl<DriverInfoMapper, DriverInfo> implements DriverInfoService {

    @Autowired
    private WxMaService wxMaService;

    @Autowired
    private DriverSetMapper driverSetMapper;

    @Autowired
    private DriverAccountMapper driverAccountMapper;

    @Autowired
    private DriverLoginLogMapper driverLoginLogMapper;

    @Autowired
    private HttpServletRequest request;

    @Override
    public Long login(String code) {
        //1.调用微信Api 获取唯一表示 openid
        String openid = null;
        WxMaJscode2SessionResult sessionInfo = null;
        try {
            sessionInfo = wxMaService.getUserService().getSessionInfo(code);
            openid = sessionInfo.getOpenid();

            //2.根据 openid 查询数据库，判定第一次登录
            DriverInfo driverInfo = this.getOne(
                    new LambdaQueryWrapper<DriverInfo>().eq(DriverInfo::getWxOpenId, openid));

            if (driverInfo == null) {
                //3.如果第一次登录，添加司机基本信息 DriverInfo
                driverInfo = new DriverInfo();
                driverInfo.setNickname(String.valueOf(System.currentTimeMillis()));
                driverInfo.setAvatarUrl("https://cdn.jsdelivr.net/gh/lingzhexi/blogImage/post/rose_pic.jpg");
                driverInfo.setWxOpenId(openid);
                this.save(driverInfo);

                //4.初始化司机相关设置（接单范围等） DriverSet
                DriverSet driverSet = new DriverSet();
                driverSet.setDriverId(driverInfo.getId());
                driverSet.setOrderDistance(BigDecimal.valueOf(0));//0 无限制
                driverSet.setAcceptDistance(BigDecimal.valueOf(SystemConstant.ACCEPT_DISTANCE));//5公里
                driverSet.setIsAutoAccept(0);// 0：否 1：是
                driverSetMapper.insert(driverSet);

                //5.初始化司机账户信息 DriverAccount
                DriverAccount driverAccount = new DriverAccount();
                driverAccount.setDriverId(driverInfo.getId());
                driverAccountMapper.insert(driverAccount);

            }
            //6.添加司机登录信息 DriverLoginLog
            DriverLoginLog driverLoginLog = new DriverLoginLog();
            driverLoginLog.setDriverId(driverInfo.getId());
            driverLoginLog.setIpaddr(request.getRemoteAddr());
            driverLoginLog.setMsg("小程序登录");
            driverLoginLogMapper.insert(driverLoginLog);


            //7.返回司机Id
            return driverInfo.getId();
        } catch (WxErrorException e) {
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
    }

    @Override
    public DriverLoginVo getDriverLoginInfo(Long driverId) {
        //1.查询司机基本信息
        DriverInfo driverInfo = this.getById(driverId);

        //2.查询数据封装vo DriverLoginVo
        DriverLoginVo driverLoginVo = new DriverLoginVo();
        BeanUtils.copyProperties(driverInfo, driverLoginVo);

        //3.判断是否需要创建人脸模型
        Boolean isArchiveFace = StringUtils.hasText(driverInfo.getFaceModelId());
        driverLoginVo.setIsArchiveFace(isArchiveFace);

        //4.返回vo
        return driverLoginVo;
    }

}