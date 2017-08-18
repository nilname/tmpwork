package com.hx.bigdata

import java.text.SimpleDateFormat
import java.util.Calendar

import org.apache.spark.sql.{Column, DataFrame, DataFrameNaFunctions, SparkSession}

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
  def getCarsfromRegion(cardf: DataFrame, regiondf: DataFrame, sparkSession: SparkSession): DataFrame = {
    import sparkSession.implicits._
    val tmpdf = regiondf.agg(Map("pos_lon" -> "min", "pos_lon" -> "max", "pos_lat" -> "min", "pos_lat" -> "max")).takeAsList(1).get(0)
    val a = tmpdf.getDouble(0)
    val b = tmpdf.getDouble(0)
    val c = tmpdf.getDouble(0)
    val d = tmpdf.getDouble(0)

    val middf = cardf.filter($"pos_lon" > a).filter($"pos_lon" < b).filter($"pos_lon" > c).filter($"pos_lon" < d)
    val final_df = middf.groupBy("carno").count().join(middf, "carno").filter("count >10")
    final_df
  }

  def getCarsfromroad(cardf: DataFrame, roaddf: DataFrame, sparkSession: SparkSession): DataFrame = {
    import sparkSession.implicits._
    val road_len = roaddf.count().toInt
    val tmplist = roaddf.take(road_len)
    var a: Double = 0
    var b: Double = 0
    var c: Double = 0
    var d: Double = 0
    var middf: DataFrame = null
    var final_df: DataFrame = null
    for (i <- 0 to road_len) {
      a = tmplist(i).getDouble(0) + 0.0001
      b = tmplist(i).getDouble(0) - 0.0001
      c = tmplist(i).getDouble(1) + 0.0001
      d = tmplist(i).getDouble(1) - 0.0001
      middf = cardf.filter($"pos_lon" > a).filter($"pos_lon" < b).filter($"pos_lon" > c).filter($"pos_lon" < d)
      if (i == 0) {
        final_df = middf
      } else {
        final_df = final_df.union(middf)
      }
    }
    final_df
  }

}
