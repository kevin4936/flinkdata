# flinkdata

《如何计算实时热门商品》scala版

Run the application

$ sbt

sbt:root> project server

sbt:server> ~reStart

本案例将实现一个“实时热门商品”的需求,我们可以将“实时热门商品”翻译成程序员更好理解的需求:

每隔 5 分钟输出最近一小时内点击量最多的前 N 个商品。

将这个需求进行分解我们大概要做这么几件事情:

• 抽取出业务时间戳,告诉 Flink 框架基于业务时间做窗口

• 过滤出点击行为数据

• 按一小时的窗口大小,每 5 分钟统计一次,做滑动窗口聚合(Sliding Window)

• 按每个窗口聚合,输出每个窗口中点击量前 N 名的商品

本案例改写于《Apache Flink 零基础实战教程:如何计算实时热门商品 作者 伍翀 java版代码》：

https://github.com/wuchong/my-flink-project/blob/master/src/main/java/myflink/HotItems.java






