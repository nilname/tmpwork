package com.hx.bigdata

import java.text.SimpleDateFormat
import java.util.Calendar

/**
  * Created by fangqing on 8/14/17.
  */
object getStatus {
//  def main(args: Array[String]): Unit = {
//    print("hello")
//    print(getLastNminute(5))
//  }
  def getLastNminute(num:Int):String= {
    var dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    var cal: Calendar = Calendar.getInstance()
    cal.add(Calendar.MINUTE, -num)
    var yesterday = dateFormat.format(cal.getTime())
    yesterday
  }
  def getNminuteLater(num:Int):String= {
    var dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    var cal: Calendar = Calendar.getInstance()
    cal.add(Calendar.MINUTE, num)
    var yesterday = dateFormat.format(cal.getTime())
    yesterday
  }
}
