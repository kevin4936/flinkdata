package com.kevin.flinkdata.beans

/** 用户行为数据结构 **/
// 用户ID
// 商品ID
// 商品类目ID
// 用户行为, 包括("pv", "buy", "cart", "fav")
// 行为发生的时间戳，单位秒

// case class UserBehavior(
//     userId: String,
//     itemId: String,
//     categoryId: String,
//     behavior: String,
//     timestamp: Long
// )

@SerialVersionUID(1)
class UserBehavior(
    var userId: String,
    var itemId: String,
    var categoryId: String,
    var behavior: String,
    var timestamp: Long
) extends Serializable {
    def this(){
        this("Nothing", "Nothing", "Nothing", "Nothing", 0)
    }

    override def toString(): String = s"userId:${userId},itemId:${itemId},categoryId:${categoryId},behavior:${behavior},timestamp:${timestamp}"

}
