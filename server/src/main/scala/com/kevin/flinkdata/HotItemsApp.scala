package com.kevin.flinkdata

import com.kevin.flinkdata.beans.UserBehavior
import com.kevin.flinkdata.functions.CountAgg
import com.kevin.flinkdata.functions.TopNHotItems
import com.kevin.flinkdata.functions.WindowResultFunction
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.common.functions.FilterFunction
import org.apache.flink.api.common.state.ListState
import org.apache.flink.api.common.state.ListStateDescriptor
import org.apache.flink.api.java.io.PojoCsvInputFormat
import org.apache.flink.api.java.tuple.Tuple
import org.apache.flink.api.java.tuple.Tuple1
import org.apache.flink.api.java.typeutils.GenericTypeInfo
import org.apache.flink.api.java.typeutils.PojoTypeInfo
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.configuration.Configuration
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

import java.io.File
import java.net.URL
import java.sql.Timestamp
import java.util.ArrayList
import java.util.Comparator
import java.util.List

//https://github.com/wuchong/my-flink-project/blob/master/src/main/java/myflink/HotItems.java

object HotItemsApp {

  def start: Unit = {

    // 创建 execution environment
    //val env = StreamExecutionEnvironment.getExecutionEnvironment
    val env = StreamExecutionEnvironment.createLocalEnvironment()
    // 告诉系统按照 EventTime 处理
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    // 为了打印到控制台的结果不乱序，我们配置全局的并发为1，改变并发对结果正确性没有影响
    env.setParallelism(1)

    // UserBehavior.csv 的本地文件路径, 在 resources 目录下
    val fileUrl = this.getClass.getClassLoader.getResource("UserBehavior.csv")

    println("csv.path=" + fileUrl)

    val csvFile = new File(fileUrl.toURI)
    if(!csvFile.exists()) {
      throw new RuntimeException(s"csv ${fileUrl} file is not found")
    }

    val filePath = Path.fromLocalFile(csvFile)
    // 抽取 UserBehavior 的 TypeInformation，是一个 PojoTypeInfo
    val pojoType = TypeExtractor
      .createTypeInfo(classOf[UserBehavior])
      .asInstanceOf[PojoTypeInfo[UserBehavior]]
    // 由于 Java 反射抽取出的字段顺序是不确定的，需要显式指定下文件中字段的顺序
    val fieldOrder = Array[String](
      "userId",
      "itemId",
      "categoryId",
      "behavior",
      "timestamp"
    )
    // 创建 PojoCsvInputFormat
    val csvInput =
      new PojoCsvInputFormat[UserBehavior](filePath, pojoType, fieldOrder)

    env
      // 创建数据源，得到 UserBehavior 类型的 DataStream
      .createInput(csvInput, pojoType)
      // 抽取出时间和生成 watermark
      .assignTimestampsAndWatermarks(
        new AscendingTimestampExtractor[UserBehavior]() {

          override def extractAscendingTimestamp(userBehavior: UserBehavior)
              : Long = {
            // 原始数据单位秒，将其转成毫秒
            userBehavior.timestamp * 1000
          }
        }
      )
      // 过滤出只有点击的数据
      .filter(new FilterFunction[UserBehavior]() {

        override def filter(userBehavior: UserBehavior): Boolean = {
          // 过滤出只有点击的数据
          userBehavior.behavior.equals("pv")
        }
      })
      .keyBy("itemId")
      .timeWindow(Time.minutes(60), Time.minutes(5))
      .aggregate(new CountAgg, new WindowResultFunction)
      .keyBy("windowEnd")
      .process(new TopNHotItems(3))
      .print()

    env.execute("Hot Items Job");
  }

}
