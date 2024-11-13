package com.atguigu.daijia.driver.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.atguigu.daijia.common.constant.SystemConstant;
import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.driver.config.TencentConfigProperties;
import com.atguigu.daijia.driver.mapper.*;
import com.atguigu.daijia.driver.service.CosService;
import com.atguigu.daijia.driver.service.DriverInfoService;
import com.atguigu.daijia.model.entity.driver.*;
import com.atguigu.daijia.model.form.driver.DriverFaceModelForm;
import com.atguigu.daijia.model.form.driver.UpdateDriverAuthInfoForm;
import com.atguigu.daijia.model.vo.driver.DriverAuthInfoVo;
import com.atguigu.daijia.model.vo.driver.DriverLoginVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tencentcloudapi.common.AbstractModel;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.iai.v20180301.IaiClient;
import com.tencentcloudapi.iai.v20180301.models.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class DriverInfoServiceImpl extends ServiceImpl<DriverInfoMapper, DriverInfo> implements DriverInfoService {

    @Autowired
    private WxMaService wxMaService;

    @Autowired
    private DriverInfoMapper driverInfoMapper;

    @Autowired
    private DriverSetMapper driverSetMapper;

    @Autowired
    private DriverAccountMapper driverAccountMapper;

    @Autowired
    private DriverLoginLogMapper driverLoginLogMapper;

    @Autowired
    private DriverFaceRecognitionMapper driverFaceRecognitionMapper;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private CosService cosService;

    @Autowired
    private TencentConfigProperties tencentConfigProperties;

    @Override
    public Long login(String code) {
        //1.调用微信Api 获取唯一表示 openid
        String openid = null;
        WxMaJscode2SessionResult sessionInfo = null;
        try {
            sessionInfo = wxMaService.getUserService().getSessionInfo(code);
            openid = sessionInfo.getOpenid();

            //2.根据 openid 查询数据库，判定第一次登录
            LambdaQueryWrapper<DriverInfo> queryWrapper = new LambdaQueryWrapper();
            queryWrapper.eq(DriverInfo::getWxOpenId, openid);
            DriverInfo driverInfo = driverInfoMapper.selectOne(queryWrapper);

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
        DriverInfo driverInfo = driverInfoMapper.selectById(driverId);

        //2.查询数据封装vo DriverLoginVo
        DriverLoginVo driverLoginVo = new DriverLoginVo();
        BeanUtils.copyProperties(driverInfo, driverLoginVo);

        //3.判断是否需要创建人脸模型
        Boolean isArchiveFace = StringUtils.hasText(driverInfo.getFaceModelId());
        driverLoginVo.setIsArchiveFace(isArchiveFace);

        //4.返回vo
        return driverLoginVo;
    }

    @Override
    public DriverAuthInfoVo getDriverAuthInfo(Long driverId) {
        //1.查询司机基本信息
        DriverInfo driverInfo = this.getById(driverId);

        //2.查询数据封装vo DriverAuthInfoVo
        DriverAuthInfoVo driverAuthInfoVo = new DriverAuthInfoVo();
        BeanUtils.copyProperties(driverInfo, driverAuthInfoVo);

        //3.拼装 DriverAuthInfoVo
        driverAuthInfoVo.setIdcardBackShowUrl(cosService.getImageUrl(driverAuthInfoVo.getIdcardBackUrl()));
        driverAuthInfoVo.setIdcardFrontShowUrl(cosService.getImageUrl(driverAuthInfoVo.getIdcardFrontUrl()));
        driverAuthInfoVo.setIdcardHandShowUrl(cosService.getImageUrl(driverAuthInfoVo.getIdcardHandUrl()));
        driverAuthInfoVo.setDriverLicenseFrontShowUrl(cosService.getImageUrl(driverAuthInfoVo.getDriverLicenseFrontUrl()));
        driverAuthInfoVo.setDriverLicenseBackShowUrl(cosService.getImageUrl(driverAuthInfoVo.getDriverLicenseBackUrl()));
        driverAuthInfoVo.setDriverLicenseHandShowUrl(cosService.getImageUrl(driverAuthInfoVo.getDriverLicenseHandUrl()));

        //4.返回vo
        return driverAuthInfoVo;
    }

    @Override
    public Boolean updateDriverAuthInfo(UpdateDriverAuthInfoForm updateDriverAuthInfoForm) {
        //1.获取司机id
        Long driverId = updateDriverAuthInfoForm.getDriverId();
        DriverInfo driverInfo = new DriverInfo();
        driverInfo.setId(driverId);

        //2.查询数据封装 driverInfo
        BeanUtils.copyProperties(updateDriverAuthInfoForm, driverInfo);

        //3.更新 driverInfo
        return this.updateById(driverInfo);
    }

    @Override
    public Boolean createDriverFaceModel(DriverFaceModelForm driverFaceModelForm) {
        // 1.查询司机基本信息
        DriverInfo driverInfo = driverInfoMapper.selectById(driverFaceModelForm.getDriverId());
        try {
            // 2.拼接CreateGroupRequest对象
            CreatePersonRequest req = getRequest(driverFaceModelForm, driverInfo);

            // 3.返回的resp是一个CreateGroupResponse的实例，与请求对象对应
            CreatePersonResponse resp = tencentConfigProperties.getIaiClient().CreatePerson(req);
            log.info("人脸识别返回接口数据：{}", AbstractModel.toJsonString(resp));
            String faceId = resp.getFaceId();
            // 4.判断返回是否有人脸Id
            if (StringUtils.hasText(faceId)) {
                driverInfo.setFaceModelId(faceId);
                this.updateById(driverInfo);
            }
        } catch (TencentCloudSDKException e) {
            log.error("人脸识别异常：{}", e.getMessage());
            return false;
        }
        return true;
    }


    // 获取司机设置信息
    @Override
    public DriverSet getDriverSet(Long driverId) {
        LambdaQueryWrapper<DriverSet> query = new LambdaQueryWrapper<>();
        query.eq(DriverSet::getDriverId, driverId);
        return driverSetMapper.selectOne(query);
    }

    @Override
    public Boolean isFaceRecognition(Long driverId) {
        LambdaQueryWrapper<DriverFaceRecognition> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(DriverFaceRecognition::getDriverId, driverId);
        queryWrapper.eq(DriverFaceRecognition::getFaceDate, new DateTime().toString("yyyy-MM-dd"));
        long count = driverFaceRecognitionMapper.selectCount(queryWrapper);
        return count != 0;
    }

    @Override
    public Boolean verifyDriverFace(DriverFaceModelForm driverFaceModelForm) {
        try {
            IaiClient client = tencentConfigProperties.getIaiClient();
            // 生成验证人脸请求对象
            VerifyFaceRequest req = new VerifyFaceRequest();
            req.setImage(driverFaceModelForm.getImageBase64());
            req.setPersonId(String.valueOf(driverFaceModelForm.getDriverId()));

            // 返回的resp是一个VerifyFaceResponse的实例，与请求对象对应
            VerifyFaceResponse resp = client.VerifyFace(req);
            // 输出json格式的字符串回包
            System.out.println(AbstractModel.toJsonString(resp));
            if (resp.getIsMatch()) {//照片对比成功
                //2.如果照片对比成功，静态活体活体检测
                Boolean isSuccess = this.detectLiveFace(driverFaceModelForm.getImageBase64());
                if (isSuccess) {//3.静态活体检测通过，添加数据到认证表里
                    DriverFaceRecognition driverFaceRecognition = new DriverFaceRecognition();
                    driverFaceRecognition.setDriverId(driverFaceModelForm.getDriverId());
                    driverFaceRecognition.setFaceDate(new Date());
                    driverFaceRecognitionMapper.insert(driverFaceRecognition);
                    return true;
                }
            }
        } catch (TencentCloudSDKException e) {
            System.out.println(e.toString());
//            throw new GuiguException(ResultCodeEnum.FACE_ERROR);
        }
        throw new GuiguException(ResultCodeEnum.FACE_ERROR);
    }

    /**
     * 人脸静态活体检测
     * @param imageBase64
     * @return
     */
    private Boolean detectLiveFace(String imageBase64) {
        try {
            IaiClient client = tencentConfigProperties.getIaiClient();
            // 实例化一个请求对象,每个接口都会对应一个request对象
            DetectLiveFaceRequest req = new DetectLiveFaceRequest();
            req.setImage(imageBase64);
            // 返回的resp是一个DetectLiveFaceResponse的实例，与请求对象对应
            DetectLiveFaceResponse resp = client.DetectLiveFace(req);

            // 输出json格式的字符串回包
            System.out.println(DetectLiveFaceResponse.toJsonString(resp));
            if(resp.getIsLiveness()) {
                return true;
            }
        } catch (Exception e) {
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        return false;

    }


    @Transactional
    @Override
    public Boolean updateServiceStatus(Long driverId, Integer status) {
        LambdaQueryWrapper<DriverSet> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(DriverSet::getDriverId, driverId);
        DriverSet driverSet = new DriverSet();
        driverSet.setServiceStatus(status);
        driverSetMapper.update(driverSet, queryWrapper);
        return true;
    }

    public CreatePersonRequest getRequest(DriverFaceModelForm driverFaceModelForm, DriverInfo driverInfo) {
        CreatePersonRequest req = new CreatePersonRequest();

        // 设置相关值
        req.setGroupId(tencentConfigProperties.getPersonGroupId());

        //基本信息111
        req.setPersonId(String.valueOf(driverInfo.getId()));
        req.setGender(Long.parseLong(driverInfo.getGender()));
        req.setQualityControl(4L);
        req.setUniquePersonControl(4L);
        req.setPersonName(driverInfo.getName());
        req.setImage(driverFaceModelForm.getImageBase64());
        return req;
    }

}