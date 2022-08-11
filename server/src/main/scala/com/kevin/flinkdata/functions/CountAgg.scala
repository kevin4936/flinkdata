package com.kevin.flinkdata.functions

import org.apache.flink.api.common.functions.AggregateFunction
import com.kevin.flinkdata.beans.UserBehavior

/** COUNT 统计的聚合函数实现，每出现一条记录加一
  */

class CountAgg extends AggregateFunction[UserBehavior, Long, Long] {

  override def createAccumulator: Long = 0L

  override def add(userBehavior: UserBehavior, acc: Long): Long = {
    acc + 1
  }

  override def getResult(acc: Long): Long = {
    acc
  }

  override def merge(acc1: Long, acc2: Long): Long = {
    acc1 + acc2
  }

}
