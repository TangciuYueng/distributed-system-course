# syn

have to **cooperate** and **synchronize** to solve the problem correctly

|缘由|举例|设计需求|主题|
|:--:|:--:|:--:|:--:|
|实体需要对事件发生的时间顺序达成一致|相机传感网络的车辆追踪, 分布式电子商务系统的商业交易操作|Entitles should have a common understanding of time across different computers|Time Synchronization|
|实体需要对分享公共资源达成一致|分布式文件系统的读写操作|Entitles should coordinate and agree on when and how to access resources|Mutual Exclusion(互斥)|

将每个节点的时钟同步成全局时钟

## 应用场景
### 追踪可疑车辆
- 相机部署到整个城市范围
- 每个相机传感器检测车辆信息, 并向中心服务器报告检测到车辆的时间信息
- 服务器追踪可疑车辆的运动轨迹

难点在于每个相机不一定都能维护统一的时间信息，就追踪出问题了
### 分布式文件系统的写操作
如果分布式的客户端在写文件的过程中不考虑写操作的同步机制, 该文件的数据内容损坏
要按顺序写，不然就一个覆盖另一个了

## Time Synchronization
### Clock Synchronization
#### Coordinated Universial Time(UTC)
- All the computers generally synchronized to the **primary time standard**
- UTC is broadcast via the **satellites** with an accuracy of 0.5 msec
- PC通过有接收器的集群获取的时间进行校准，PC没有接收器因为太贵了
- The most physical clocks use **atomic oscillators**

#### Tracking Time on a Computer
材料的老化等等都会导致电脑时钟偏移

如何衡量
![](./ref/note8-1.PNG)

$Skew = \frac{dC}{dt} - 1$

设置一个区间，最大许可漂移率(maxium drift rate)$\rho$
$1 - \rho \le \frac{dC}{dt} \le 1 + \rho$

#### Clock Synchronization Algorithm
##### Cristian's Algorithm
- basic idea
  - identify a **network time server** than has a accurate source for time(g.g. the time server has a UTC receiver)
  - All the client contact the network time server for synchronization
- 但是**网络延迟**，需要估计网络延迟然后补偿

![](./ref/note8-2.PNG)
Cli和Ser可能有误差，T1和T2误差不准确也不知道谁早
Cli将T1发送过去
Ser将T1、T2、T3发送回去

已知T1 < T4、T2 < T3

但我们Assuming than the transmission dalay from Cli to Ser and Ser to Cli are the same上行下行延迟相同
$T2 - T1 \approx T4 - T3$
Cli estimates the offset $\theta$ relative to teh Ser
$$
\theta = T3 + dTres - T4 \\
= T3 + ((T2 - T1) + (T4 - T3)) / 2 - T4 \\
= ((T2 - T1) + (T3 - T4)) / 2
$$

Then the Cli Time should be incremented or decremented by $\theta$

Instead of changing the time drastically by $\theta$ seconds, typically the time is **gradually** synchronized
一下子改变时钟太快了对计算机不好

Discussion
- Cristian's algorithm assumes that the round-trip times for message exchanged over the **network** is **reasonably short**，在局域网中，但是广域网要的时间可能不短了
- the delay for the request and response are equal
- 服务器故障或服务器时钟故障，同步就不行了
- 没办法保证客户端最大的时间漂移
##### Berkeley Algorithm
- 不依赖UTC，通过所有计算机的时间调和平均
- 一个服务器故障了，再选一个当服务器就好了
- 也是局域网中比较好，广域网就有延迟不得
- 但可能出现离群点，需要剔除

- Approach
  - A time server periodically sends its time to all the computers and polls them for the time difference
  - The computers compute the time difference and then reply
  - The server computes an **average** time difference for each computer
  - The server commands all the computers to update their time(by gradually time synchronization)
##### Network Time Protocol
广域网中能用
现在设备基本都是这样同步
**NTP** defines an architecture for a time service and a protocol to distribute time infomation over the Internet.

servers are connected in a **logical hierarchy** called **synchronization subnet**

**一般第一级root有最精密的时钟信息UTC receiver，精密度随层级下降**

![](./ref/note8-3.PNG)

只有低级的才需要与高级的进行同步，同时更新作为高级的下一层级，高级的不需要与低级同步

同步还类似Cristian' algorithm，但不同的是通过**多次采样**使用统计出来的结果

- 可以保证UTC时间的精密同步
- 可扩展性
- 可靠性
- 安全认证机制

#### Summary
物理时钟不精密，需要同步

### Logical Clock Synchronization
Logical clocks are used to define **an order of events** without measuring the physical time at which the events occurred
不关注timestamp的值，而是直接关注事件的次序
## Mutual Exclusion
## Election Algorithm