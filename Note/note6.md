# 云计算
## 困境
- 计算负载变化大
  - 峰值负载超过均值2-10倍
- 贵
  - 硬件投资成本
  - 专业知识
  - 维护成本
- scale难
  - difficult to scale up：更多及其安装和与现有的机器集成，更难的是保证服务能一直提供的同时更新系统
  - difficult to scale down：向提供的计算资源和用户吻合，用户少了资源也能少
    - 过剩硬件
    - 服务器闲置也耗电
    - fixed costs
## 电厂
任何单位均需要供电源
趋势：大型集中式电厂
电网将各电厂连接起来，为用户进行输电
电表记录用户的实际用电，根据电表记录进行付费
## 供电模型和计算模型
||供电模型|计算平台
|:--:|:--:|:--:|
|经济规模|单一大型电厂的运行效费远比多个小厂来得更高|一个大型数据中心运行效费比多个小型数据中心更高|
|多路复用|高利用率|高利用率|
|投资成本|无需投资电厂费用，现收现付模型|用户无需数据中心的建设费用，现收现付计费模型|
|可扩展性|按需供电，秒级响应|按需计算，秒级响应|

## 云计算到底是什么
Cloud computing is a model for enabling convenient, on-demand network access to a shared pool of configurable computing resources (e.g., networks, servers, storage, applications, and services) that can be rapidly provisioned and released with minimal management effort or service provider interaction.

**基本特征**
- On-demand self service按需的自服务机制
- Broad network access宽带网络访问
- Resource pooling
- Rapid elasticity
- Measured service

但也不是什么都能云

## 其他术语
- Web万维网
- Internet因特网

## 云计算类型
- SaaS
  - 云端提供完整的应用解决方案
  - 包括硬件、中间件(操作系统...)、硬件
- PaaS
  - 云端提供中间件、基础设置服务
- IaaS
  - 云端提供最基础的计算资源
  - 虚拟机、刀片服务器、硬盘...

### private/hybrid/community clouds
- 共有云Public 
  - 商业
  - 阿里云、Amazon AWS、Microsoft、Azure
- 私有云Private
  - 仅仅在一个特定的组织内部用户使用
- 社区云Comminuty
  - 多个类似组织共享的云
  - Google's Gov Cloud

## 虚拟化
云计算的动力
用一台电脑，给更多用户分配虚拟机以满足各自的需求
很灵活
### 优点
- 迁移
- 分时共享

### 不足
隔离性（其中一个用户负载突然增加

## 云计算的痛点
- 可用性Availability
  - 云平台出现故障，上层应用怎么办
- 数据入闸Data lock-in
  - 将数据从一个云平台移动到另一个云平台
- 数据保密性和安全性Data confidentiality and auditability
- 数据转移瓶颈Data transfer bottlenecks
  - 海量数据转移
  - AWS的导入导出
- 性能不可预测Performance unpredictable
- 大型分布式系统的bugs
  - 严重错误难以重现
  - 也不能给你设置断点调试