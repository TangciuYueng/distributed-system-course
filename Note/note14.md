# HDFS

## Introduction

### 用途
- 文本处理 Data-intensive text processing
- 生物信息 Assembly of large genomes
- 图挖掘 Graph mining
- 机器学习/数据挖掘 Machine learning & data mining
- 社交网络 Large scale social network analysis

### 生态系统
- Hadoop Common: Contains libraries and other modules
- HDFS: Hadoop distributed file system
- Hadoop YARN: Yet another resource negotiator
- Hadoop MapReduce: A programming model for large scale data processing

### HDFS
是Hadoop的文件系统的组成部分
- **可靠**存储very large data sets
- to stream those data sets at **high bandwidth** to user applications

通过 replicating file content on multiple machines(DataNodes)

## Architecture
HDFS（Hadoop分布式文件系统）是一种块结构的文件系统：
- 文件被分成大小为128MB的块（可以按文件进行配置）。
- 一个文件可以由多个块组成，并且它们被存储在一个或多个具有数据存储容量的机器集群中。
- 文件的每个块都会被复制到多台机器上，以防止数据丢失。

### 节点Node
NameNode（名称节点）和DataNodes（数据节点）是HDFS（Hadoop分布式文件系统）中的两个重要组件：

- **NameNode：**
  - HDFS将文件系统的元数据和应用程序数据分开存储。
  - 元数据指的是文件元数据，包括权限、修改时间、访问时间、命名空间和磁盘空间配额等属性，称为“inodes”（索引节点），以及文件所属的块列表。
  - HDFS将元数据存储在专用服务器上，称为NameNode（主节点）。
  - NameNode负责管理文件系统的命名空间和元数据信息。

- **DataNodes：**
  - 应用程序数据存储在其他服务器上，称为DataNodes（从节点）。
  - DataNodes负责存储实际的应用程序数据块。
  - 所有服务器通过基于TCP的协议（RPC）进行全连接，彼此之间进行通信。

在HDFS中，NameNode充当主服务器，负责管理文件系统的命名空间和元数据，而DataNodes充当从服务器，负责存储实际的应用程序数据块。这种分离元数据和应用程序数据的架构有助于提高系统的可扩展性和容错性。

单一NameNode（单一名称节点）在HDFS中扮演着关键角色：

- **维护命名空间树：**
  - 负责维护文件和目录的层次结构，执行诸如打开、关闭和重命名文件和目录等操作。

- **确定文件块到DataNodes的映射：**
  - 决定文件数据块在DataNodes上的物理位置，即文件数据的实际存储位置。

- **文件元数据（即“inode”）：**
  - 负责管理文件的元数据，包括文件的权限、所有者、修改时间等信息。

- **授权和认证：**
  - 处理对文件系统的授权和身份验证，确保只有经过授权的用户可以访问文件。

- **收集来自DataNodes的块报告：**
  - 收集来自数据节点的块报告，以了解数据块的位置和状态。

- **复制丢失的数据块：**
  - 当发现某个数据块丢失时，负责复制缺失的数据块，以确保数据的冗余备份。

- **将整个命名空间保存在RAM中：**
  - HDFS将整个命名空间的信息保存在内存中，从而实现对元数据的快速访问。

单一NameNode是HDFS的主服务器，负责协调和管理整个文件系统的操作，包括命名空间管理、数据块映射、元数据管理、授权认证等。由于NameNode存储了文件系统的关键信息，因此它的高可用性和容错性对整个HDFS系统的稳定性至关重要。

DataNodes（数据节点）在HDFS中有以下职责：

- **处理文件系统客户端的读写请求：**
  - DataNodes负责响应文件系统客户端的读取和写入请求，为客户端提供文件数据的读取和写入服务。

- **执行块的创建、删除和复制：**
  - DataNodes根据来自NameNode的指令，执行数据块的创建、删除和复制操作。这包括在需要时创建新的数据块、删除不再需要的数据块，以及复制缺失的数据块以保证数据的冗余备份。

- **定期向NameNode发送块报告：**
  - DataNodes定期向NameNode发送块报告。块报告包含了该DataNode所存储的所有数据块的信息，以及这些块的状态和健康状况。这有助于NameNode了解整个文件系统中数据块的分布和可用性情况，以便进行合理的块管理。

### Heartbeat
NameNode和DataNode之间的通信包括心跳（Heartbeats）。

**心跳通信：**
DataNodes定期向NameNode发送心跳以确认DataNode的运行状态，并确认它所托管的数据块副本是否可用。

heartbeat是指DataNode定期向NameNode发送的简短消息，以表明它仍然处于运行状态。这种定期的心跳通信允许NameNode监测和维护整个HDFS集群的健康状况。通过收到DataNodes的心跳，NameNode可以确认各个DataNode的运行状态，并及时检测到任何可能的故障或数据块不可用的情况。这种机制有助于保持HDFS系统的可靠性和稳定性。

发送规则
- Data Node sends Heartbeats
- Every 10th heartbeats is a Block report 如果每次都发 block report 就网络占用带宽太大了
- Name Node builds metadata from Block reports
- TCP - every 3 seconds

### Block reports
DataNode通过发送块报告（block report）向NameNode标识其拥有的块副本。块报告包含每个服务器托管的块副本的块ID、生成戳（generation stamp）和长度。

- **块报告：**
  - DataNode通过发送块报告向NameNode提供有关其存储的块副本的信息。
  - 块报告包含了每个块副本的关键信息，如块ID、生成戳和长度等。
  
- **提供最新的集群块副本位置视图：**
  - 块报告使NameNode能够获取关于整个集群中块副本位置的最新视图。
  - NameNode使用块报告中的信息构建和维护最新的元数据，包括块的位置、状态和长度等。

通过定期发送块报告，DataNode确保NameNode获得了关于存储在集群中的块副本的实时信息。这使得NameNode能够动态地维护文件系统的元数据，确保其具有准确和最新的关于块副本的信息，从而提高了整个HDFS系统的可靠性和一致性。

### Failure recovery
NameNode不直接调用DataNodes。它通过心跳的回复向DataNodes发送指令。

- **使用心跳回复发送指令：**
  - NameNode通过解析来自DataNodes的心跳回复，向DataNodes发送指令，实现对数据节点的管理和控制。

- **指令包括以下命令：**
  - 复制块到其他节点：
    - 当某个DataNode发生故障（死机）时，NameNode可以通过指令要求其他节点复制该块，以保持数据的冗余备份。
    - 将数据块复制到其他节点，以确保数据的可靠性。

  - 删除本地块副本：
    - 当某个数据块的复制数目过多或需要调整块分布时，NameNode可以通过指令要求DataNode删除本地的某些块副本。

  - 重新注册或关闭节点：
    - 当DataNode需要重新注册或关闭时，NameNode可以通过指令通知相应的DataNode执行相应的操作。

当DataNode发生故障（死机）时，NameNode会察觉到并指示其他DataNode将数据复制到新的DataNode。

- **如果NameNode发生故障：**
  - 如果NameNode发生故障，整个HDFS系统可能会面临严重问题，因为NameNode负责管理文件系统的命名空间和元数据。
  - 在HDFS中，NameNode是单点故障（Single Point of Failure），因为如果NameNode失效，整个文件系统的操作将受到影响。

- **故障恢复机制：**
  - 为了应对NameNode的故障，Hadoop引入了称为Secondary NameNode的辅助节点，用于定期合并编辑日志（edit log）并创建检查点（checkpoint）。这有助于减轻在NameNode故障时的数据恢复负担。
  - 当NameNode发生故障时，管理员可以手动或使用自动故障切换（failover）机制将一个备用NameNode切换为主要NameNode，以维持文件系统的可用性。
  - Hadoop的HA（High Availability）配置可以使用多个NameNode实例，通过使用共享存储或使用Quorum Journal Manager来共享元数据，以实现更可靠的系统。


保持日志（metadata的修改日志）。
- **日志（Journal）：** 这是元数据修改的持久记录。
  - 在HDFS中，编辑日志（journal）是对元数据更改的详细记录，包括对文件和目录的创建、删除、重命名等操作。
  - 通过保留日志，HDFS可以追踪和记录系统的状态变化，以便在需要时进行故障恢复和维护一致性。
- **CheckpointNode（检查点节点）：**
  - 当编辑日志（journal）变得过长时，CheckpointNode负责合并现有的检查点和日志，创建一个新的检查点和一个空的日志。
  - CheckpointNode的作用包括架构（Architecture）方面和故障恢复（Failure recovery）方面。
  - 在软件升级和文件系统快照方面有用。通过创建文件系统快照，管理员可以持久保存文件系统（包括数据和元数据）的当前状态，以便在升级过程中出现数据丢失或损坏时，可以回滚升级并将HDFS还原到快照时的命名空间和存储状态。

- **BackupNode（备份节点）：**
  - BackupNode是一个只读的NameNode。
  - 它维护一个内存中的、始终与NameNode的状态同步的文件系统命名空间镜像。
  - 如果NameNode发生故障，BackupNode的内存中的镜像和磁盘上的检查点是最新命名空间状态的记录。

## File I/O Operation and Replica Management
在大型集群中，将所有节点连接在一个扁平的拓扑结构中可能不太实际。因此，通常的做法是将节点分散到多个机架上。每个机架上的节点共享一个交换机，而机架交换机则通过一个或多个核心交换机相连。不同机架上的两个节点之间的通信需要经过多个交换机。

Hadoop引入了“Rack Awareness”（机架感知）的概念来处理这种机架结构。默认的HDFS副本放置策略可以总结如下：
1. 没有一个Datanode包含任何块的多个副本。
2. 没有一个机架包含同一块的超过两个副本，前提是集群中有足够的机架。

文件I/O操作和副本管理的过程可以描述如下：
1. 当需要新的数据块时，NameNode为其分配一个具有唯一块ID的块，并确定用于存储块副本的DataNodes列表。
2. DataNodes形成一个管道，其顺序使得从客户端到最后一个DataNode的总网络距离最小。
3. 数据以字节序列的形式推送到管道中。应用程序首先在客户端端缓冲写入的字节。当一个数据包缓冲区填满时（通常是64 KB），数据将被推送到管道。

对于文件的每个块，可以通过将数据分散到更多的机器上来提高并行处理能力和加快处理速度。这也是构建大型、宽广集群的动机之一。为了实现更快的数据处理，更多的数据块可以分布在更多的CPU核心和磁盘驱动器上。

另外，文中提到了Balancer的概念，用于解决在向Hadoop集群添加新机架时可能导致集群不平衡的情况。Balancer会检查节点之间可用存储的差异，并尝试在节点之间提供平衡。它会将数据从可用空间较少的节点复制到可用空间较多的新节点上，以实现负载均衡。

## Future Work