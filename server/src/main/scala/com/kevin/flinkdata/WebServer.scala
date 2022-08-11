package com.kevin.flinkdata

 import java.time.{LocalDateTime}

object WebServer {

  def main(args: Array[String]): Unit = {
    HotItemsApp.start
    val time = LocalDateTime.now()
    val result = time.toString
    println(s"Web Server online at $result")
  }
}
