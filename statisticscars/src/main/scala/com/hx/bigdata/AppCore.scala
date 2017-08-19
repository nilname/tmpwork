package com.hx.bigdata

import org.apache.spark.sql.{DataFrame, SparkSession}


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
      .option("url", Constant.DBURL+Constant.RESULTDB)
      .option("dbtable", s"(select * from ${Constant.TAXIGPS_TABLE} where pos_time between  \'${start}\' and \'${end}\' ) as tmp_tb ")
      .option("user", Constant.DBUSER)
      .option("password", Constant.DBPASSWD)
      .load()


    val regions = getStatus.getRegionInfo(spark)
    var tmpdf: DataFrame = null
    for (i <- 0 until regions.size) {
      tmpdf = null
      if (getStatus.getRegionType(regions(i)).eq(Constant.REGION_FLAG)) {
        tmpdf = getStatus.getCarsfromRegion(jdbcDF, regions(i), spark)
      }
      else {
        tmpdf = getStatus.getCarsfromroad(jdbcDF, regions(i), spark)
      }
      if (tmpdf != null)
        getStatus.saveResult(tmpdf, spark)

    }


    //    val now: Date = new Date()
    //    val dateFormat: SimpleDateFormat = new SimpleDateFormat(Constant.TIME_FORMATE)
    //    val compute_time = dateFormat.format(now)


    //    val car_numbers=jdbcDF.count().toInt;


    //    val tmp = results(0, compute_time, car_numbers)
    //    val res = Seq(tmp)
    //    val resDf = spark.createDataFrame(res);
    //    resDf.write.mode("append")
    //      .format("jdbc")
    //      .option("url", Constant.DBURL)
    //      .option("dbtable", Constant.RESULT_TABLE)
    //      .option("user", Constant.DBUSER)
    //      .option("password", Constant.DBPASSWD)
    //      .save()

  }


}

//case class results(id: Long, compute_time: String, aggregated_quantity: Int)

