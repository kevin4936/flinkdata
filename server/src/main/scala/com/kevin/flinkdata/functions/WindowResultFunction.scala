package com.kevin.flinkdata.functions

import org.apache.flink.streaming.api.functions.windowing.WindowFunction
import org.apache.flink.api.java.tuple.Tuple
import org.apache.flink.api.java.tuple.Tuple1
import org.apache.flink.util.Collector
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import com.kevin.flinkdata.beans.ItemViewCount


/** 用于输出窗口的结果 */
class WindowResultFunction extends WindowFunction[Long, ItemViewCount, Tuple, TimeWindow] {

	// 窗口的主键，即 itemId
	// 窗口
	// 聚合函数的结果，即 count 值
	// 输出类型为 ItemViewCount

  override def apply(
      key: Tuple,
      window: TimeWindow,
      aggregateResult: java.lang.Iterable[Long],
      collector: Collector[ItemViewCount]
  ): Unit = {
    val itemId = key.asInstanceOf[Tuple1[String]].f0
    val count = aggregateResult.iterator.next
    collector.collect(ItemViewCount.of(itemId, window.getEnd, count))
  }
}
