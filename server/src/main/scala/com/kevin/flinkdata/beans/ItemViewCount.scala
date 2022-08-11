package com.kevin.flinkdata.beans

/** 商品点击量(窗口操作的输出类型) */
// 商品ID
// 窗口结束时间戳
// 商品的点击量

@SerialVersionUID(1)
class ItemViewCount(var itemId: String, var windowEnd: Long, var viewCount: Long) extends Serializable {

    def this(){
        this("Nothing", 0, 0)
    }

    override def toString(): String = s"itemId:${itemId},windowEnd:${windowEnd},viewCount:${viewCount}"

}

object ItemViewCount {

  def of(itemId: String, windowEnd: Long, viewCount: Long): ItemViewCount = {
    new ItemViewCount(itemId, windowEnd, viewCount)
  }
  
}
