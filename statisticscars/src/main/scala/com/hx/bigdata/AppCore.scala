package com.hx.bigdata

import java.text.SimpleDateFormat
import java.util.Date

import org.apache.spark.sql.SparkSession


///**
//  * Created by fangqing on 8/14/17.
//  */
object AppCore {
  def main(args: Array[String]): Unit = {

    val start = getStatus.getLastNminute(Constant.CALCULATE_INTERVAL)
    val end = getStatus.getLastNminute(0)
    val spark = SparkSession
      .builder()
      .appName(Constant.APP_NAME)
      .getOrCreate()

    val jdbcDF = spark.read
      .format("jdbc")
      .option("url", Constant.DBURL)
      .option("dbtable", s"(select pos_lat, pos_lon from ${Constant.TAXIGPS_TABLE} where pos_time between  \'${start}\' and \'${end}\' ) as tmp_tb ")
      .option("user", Constant.DBUSER)
      .option("password", Constant.DBPASSWD)
      .load()


    var now: Date = new Date()
    var dateFormat: SimpleDateFormat = new SimpleDateFormat(Constant.TIME_FORMATE)
    var hehe = dateFormat.format(now)


    val tmp = results(0, hehe, jdbcDF.count().toInt)
    val res = Seq(tmp)
    val resDf = spark.createDataFrame(res);
    resDf.write.mode("append")
      .format("jdbc")
      .option("url", Constant.DBURL)
      .option("dbtable", Constant.RESULT_TABLE)
      .option("user", Constant.DBUSER)
      .option("password", Constant.DBPASSWD)
      .save()

  }


}

case class results(id: Long, compute_time: String, aggregated_quantity: Int)

