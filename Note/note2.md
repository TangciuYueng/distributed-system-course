# 资源共享
分布式中重要的

## 资源
- 硬件
- 软件

## 服务
一系列方法操作

## 共性需求
- Namespace for identification
  - 域名DNS管理
- Name translation to network address
  - 映射到IP地址
- Synchronization of multiple access

## Challenges
- Heterogeneity异构型
  - 允许各种类型的OS、设备等等
  - mobile code
    - 例如从服务器下载到客户端的JS
    - 利用sandbox防止JS获取客户端的隐私信息
- Openness
- Security
- Scalability
  - 可以扩展大量资源处理双十一的交易量
  - 也可以减少资源处理平时的交易量
- Failure handling
- Concurrency
- Transparency
- Quality of service