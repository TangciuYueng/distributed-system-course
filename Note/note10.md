# Replication & Consistency
多个计算机维护同一份数据

## why
- improving performance
  - caching webpages at the client browser
  - caching IP addresses at client and DNS Name Servers
  - caching in Content Delivery Network(CDNs)内容分发网络
    - 我在上海请求北京的服务器，我的舍友也在请求北京的服务器
    - 有了CDN，请求结果缓存在华东的服务器里面
    - 降低网络带宽的负载
    - 加快访问速度
- increasing the availability of services高可用性
  - 不存在单点故障问题，北京的服务器宕机了，我还可以从华东服务器请求
- enhancing the scalability of the system提高可扩展性
  - 副本多/少了提供更多/少的服务能力
- securing against malicious attacks
  - 银行账户保存一百万，黑客攻击一个花费八十万
  - 有了多个副本，攻击了一个改成一毛钱，占多数的是一百万

## 麻烦
想要维护数据一致性

## Consistency Models
### Data-Centric Consistency Models
### Client-Centric Consistency Models

## Replica Management

## Consistency Protocols