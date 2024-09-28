# 乐尚代驾项目介绍

## 1、介绍

【**乐尚代驾**】代驾是一种新型的出行服务模式，主营业务：酒后*代驾*、商务代驾、长途代驾，其主要特点是通过线上平台为用户提供代驾服务，伴随中国家庭汽车保有量的飞速增长，互联网代驾行业驶进了快车道，当前项目就是以此为背景设计出来的。



## 2、核心技术

- **SpringBoot**：简化Spring应用的初始搭建以及开发过程
- **SpringCloud**：基于Spring Boot实现的云原生应用开发工具，SpringCloud使用的技术：（Spring Cloud Gateway、Spring Cloud Task和Spring Cloud Feign等）
- **SpringBoot+SpringCloudAlibaba(Nacos，Sentinel) + OpenFeign + Gateway**
- MyBatis-Plus：持久层框架，也依赖mybatis
- Redis：内存做缓存
- Redisson：基于redis的Java驻内存数据网格 - 框架；操作redis的框架
- MongoDB: 分布式文件存储的数据库
- RabbitMQ：消息中间件；大型分布式项目是标配；分布式事务最终一致性
- Seata：分布式事务
- Drools：规则引擎，计算预估费用、取消费用等等
- GEO：GPS分区定位计算
- ThreadPoolExecutor+CompletableFuture：异步编排，线程池来实现异步操作，提高效率
- XXL-JOB: 分布式定时任务调用中心
- Knife4J/YAPI：Api接口文档工具
- MinIO（私有化对象存储集群）：分布式文件存储 类似于OSS（公有）
- 微信支付：微信支付与微信分账
- MySQL：关系型数据库 {shardingSphere-jdbc 进行读写分离; 分库，分表}
- Lombok: 实体类的中get/set 生成的jar包
- Natapp：内网穿透
- Docker：容器化技术;  生产环境Redis（运维人员）；快速搭建环境Docker run
- Git：代码管理工具；Git使用，拉代码、提交、推送、合并、冲突解决

前端技术栈

- UniApp
- Vue3全家桶
- TypeScript
- GraceUI
- UniUI
- uniapp-axios-adapter



## 3、使用的云服务

因为我们开发的是打车类的微信小程序项目，因此要使用了大量的云服务（腾讯云或者其它云都可以）与微信小程序插件，列举如下：

| 序号 | 云服务名称             | 具体用途                                                 |
| :--- | :--------------------- | :------------------------------------------------------- |
| 1    | 对象存储服务（COS）    | 存储司机实名认证的身份证和驾驶证照片等隐私图片           |
| 2    | 人脸识别（AiFace）     | 每天司机接单前的身份核实，并且具备静态活体检测功能       |
| 3    | 人员库管理（face-lib） | 云端存储司机注册时候的人脸模型，用于身份比对使用         |
| 4    | 数据万象（ci）         | 用于监控大家录音文本内容，判断是否包含暴力和色情         |
| 5    | OCR证件识别            | 用于OCR识别和扫描身份证和驾驶证的信息                    |
| 6    | 微信同声传译插件       | 把文字合成语音，播报订单；把录音转换成文本，用于安全监控 |
| 7    | 路线规划插件           | 用于规划司机下班回家的线路，或者小程序订单显示的路线     |
| 8    | 地图选点插件           | 用于小程序上面地图选点操作                               |
| 9    | 腾讯位置服务           | 路线规划、定位导航、里程和时间预估                       |



## 4、技术架构图

![技术架构图](https://cdn.jsdelivr.net/gh/lingzhexi/blogImage/post/202409240943863.png)



## 5、业务流程图

![业务流程图](https://cdn.jsdelivr.net/gh/lingzhexi/blogImage/post/202409240943809.png)

## 5、项目模块

最终服务器端架构模块

daijia-parent：根目录，管理子模块：

	common：公共类父模块

		common-log：系统日志管理

		common-util：核心工具类

		rabbit-util：service模块工具类

		service-util：service模块工具类

		spring-security：spring-security业务模块

	model：实体类模块

	server-gateway：网关

	service：service服务父模块

		service-coupon：优惠券服务模块

		service-customer：乘客服务模块

		service-dispatch：调度服务模块

		service-driver：司机服务模块

		service-map：地图服务模块

		service-mq：mq测试服务模块

		service-order：订单服务模块

		service-payment：支付服务模块

		service-rules：规则服务模块

		service-system：系统服务模块

	service-client：远程调用服务父模块

		service-coupon-client：优惠券服务远程接口

		service-customer-client：客户服务远程接口

		service-dispatch-client：调度服务远程接口

		service-driver-client：司机服务远程接口

		service-map-client：地图服务远程接口

		service-order-client：订单服务远程接口

		service-payment-client：支付服务远程接口

		service-rules-client：规则服务远程接口

		service-system-client：系统服务远程接口

	web：前端系统父模块

		web-customer：乘客端web系统

		web-driver：司机端web系统

		web-mgr：管理端web系统