# pbftSimulator
# PBFT模拟器
## 主要功能
- 实现了PBFT的主要功能，包括Request、Preprepare、Prepare、Commit、CheckPoint、ViewChange、NewView等各阶段
- 采用消息优先队列用单线程模拟多个节点之间的共识交互过程，考虑了节点之间的网络延迟（网络延迟随消息的数量增长而指数增加）
- 可以模拟包含诚实节点与拜占庭节点/掉线节点的共识交互的过程
- 可以方便地调整系统参数（节点数量，恶意节点数量等）、网络参数（网络延迟策略）以测评不同参数下PBFT的性能变化
- 可以方便地实现各种拜占庭行为，并模拟该行为对共识过程的影响
## 目录结构
- replica目录 包含3个类，分别是诚实节点Replica、掉线节点OfflineReplica和拜占庭节点ByztReplica
- Message目录 包括各种消息类型
- Simulator main函数入口，初始化模拟环境
