package com.hx.bigdata

import java.text.SimpleDateFormat
import java.util.{Date, Properties}

import org.apache.spark.sql.SparkSession
import org.apache.spark._

///**
//  * Created by fangqing on 8/14/17.
//  */
object AppCore {
  def main(args: Array[String]): Unit = {

    val start = getStatus.getLastNminute(15)
    val end = getStatus.getLastNminute(0)
    val spark = SparkSession
      .builder()
      .appName("statistics cars")
      .getOrCreate()

    val connectionProperties = new Properties()
    connectionProperties.put("user", "zfw")
    connectionProperties.put("password", "123456")
    val jdbcDF = spark.read //.jdbc("jdbc:mysql://10.10.60.196:3306/test12","taxigps",connectionProperties)
      .format("jdbc")
      .option("url", "jdbc:mysql://10.10.60.196:3306/test12")
      .option("dbtable", s"(select pos_lat, pos_lon from taxigps where pos_time between  \'${start}\' and \'${end}\' ) as tmp_tb ")
      .option("user", "zfw")
      .option("password", "123456")
      .load()


    var now: Date = new Date()
    var dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    var hehe = dateFormat.format(now)


    val tmp = results(0, hehe, jdbcDF.count().toInt)
    val res = Seq(tmp)
    val resDf = spark.createDataFrame(res);
    resDf.write.mode("append")
      .format("jdbc")
      .option("url", "jdbc:mysql://10.10.60.196:3306/test12")
      .option("dbtable", "taxigps_statistic")
      .option("user", "zfw")
      .option("password", "123456")
      .save()

//    print("=======================")

  }
}

case class results(id: Long, call_time: String, car_nums: Int)

