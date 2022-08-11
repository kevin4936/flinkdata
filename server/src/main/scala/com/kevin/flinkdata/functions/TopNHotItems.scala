package com.kevin.flinkdata.functions


import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.api.java.tuple.Tuple
import org.apache.flink.api.java.tuple.Tuple1
import org.apache.flink.util.Collector
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import com.kevin.flinkdata.beans.ItemViewCount
import org.apache.flink.api.common.state.ListState
import org.apache.flink.api.common.state.ListStateDescriptor
import org.apache.flink.configuration.Configuration
import java.sql.Timestamp
import scala.collection.mutable.ListBuffer
import org.apache.flink.streaming.api.functions.aggregation.Comparator
import scala.collection.JavaConverters._


/** 
求某个窗口中前 N 名的热门点击商品，key 为窗口时间戳，输出为 TopN 的结果字符串 
 * 
*/

class TopNHotItems(topSize: Int) extends KeyedProcessFunction[Tuple, ItemViewCount, String] {

		// 用于存储商品与点击数的状态，待收齐同一个窗口的数据后，再触发 TopN 计算
		var itemState: ListState[ItemViewCount] = _

		override def open(parameters: Configuration): Unit = {
			super.open(parameters)
			val itemsStateDesc = new ListStateDescriptor[ItemViewCount](
				"itemState-state",
				classOf[ItemViewCount])
			itemState = getRuntimeContext.getListState(itemsStateDesc)
		}

		
		override def processElement(
			input: ItemViewCount,
			 context: KeyedProcessFunction[Tuple, ItemViewCount, String]#Context,
			 collector: Collector[String]): Unit = {
			// 每条数据都保存到状态中
			itemState.add(input)
			// 注册 windowEnd+1 的 EventTime Timer, 当触发时，说明收齐了属于windowEnd窗口的所有商品数据
			context.timerService.registerEventTimeTimer(input.windowEnd + 1)
		}

		override def onTimer(
			timestamp: Long, ctx: KeyedProcessFunction[Tuple, ItemViewCount, String]#OnTimerContext, out: Collector[String]): Unit = {
			// 获取收到的所有商品点击量
			val allItems = new ListBuffer[ItemViewCount]
			for (item <- itemState.get.asScala) {
				allItems.append(item)
			}
			// 提前清除状态中的数据，释放空间
			itemState.clear
			// 按照点击量从大到小排序
			allItems.sortWith((o1, o2) => (o2.viewCount - o1.viewCount).toInt < 0)
			// 将排名信息格式化成 String, 便于打印
			val result = new StringBuilder
			result.append("====================================\n")
			result.append("时间: ").append(new Timestamp(timestamp-1)).append("\n")
            for (i <- 0 until allItems.size if i < topSize) {
				val currentItem = allItems(i)
				// No1:  商品ID=12224  浏览量=2413
				result.append("No").append(i).append(":")
					.append("  商品ID=").append(currentItem.itemId)
					.append("  浏览量=").append(currentItem.viewCount)
					.append("\n")
			}
			result.append("====================================\n\n")

			// 控制输出频率，模拟实时滚动结果
			Thread.sleep(1000)

			out.collect(result.toString)
		}
	}