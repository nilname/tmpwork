package com.hx.bigdata

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.slf4j.LoggerFactory


///**
//  * Created by fangqing on 8/14/17.
//  */
object AppCore {
  val LOG = LoggerFactory.getLogger(tmpTest.getClass);

  def main(args: Array[String]): Unit = {
    var start = "";
    var end = "";
    if (args.size == 0) {
      start = getStatus.getLastNminute(Constant.CALCULATE_INTERVAL)
      end = getStatus.getLastNminute(0)
    }
    else {
      start = args(0)
      end = args(1)
    }
    val spark = SparkSession
      .builder()
      .appName(Constant.APP_NAME)
      .getOrCreate()


    val jdbcDF = spark.read
      .format("jdbc")
      .option("url", Constant.DBURL + Constant.RESULTDB)
      .option("dbtable", s"(select * from ${Constant.TAXIGPS_TABLE} where pos_time between  \'${start}\' and \'${end}\' ) as tmp_tb ")
      .option("user", Constant.DBUSER)
      .option("password", Constant.DBPASSWD)
      .load()
    //    val jdbcDF = spark.read
    //      .format("jdbc")
    //      .option("url", Constant.DBURL + Constant.RESULTDB)
    //      .option("dbtable", Constant.TAXIGPS_TABLE)
    //      .option("user", Constant.DBUSER)
    //      .option("password", Constant.DBPASSWD)
    //      .load()
    println(s"jdbc count is ${jdbcDF.count()}")
    val regions = getStatus.getRegionInfo(spark)
    var tmpdf: DataFrame = null
    var regionID: Long = 0
    for (i <- 0 until regions.size) {
      regionID = regions(i).select("bh").takeAsList(1).get(0).getString(0).toLong
      tmpdf = null
      val flag = getStatus.getRegionType(regions(i)).trim()
      val trflag = flag.equals(Constant.REGION_FLAG.trim)
      LOG.info(s"==>this is $flag")
      LOG.info(s"==>this is ${Constant.REGION_FLAG.trim()}")
      LOG.info(s"==>this is ${trflag}")

      if (trflag) {
        println("tttttttttttttttttttttttttttt")
        tmpdf = getStatus.getCarsfromRegion(jdbcDF, regions(i), spark)
      }
      else {
        tmpdf = getStatus.getCarsfromroad(jdbcDF, regions(i), spark)
      }
      //      tmpdf = getStatus.getCarsfromRegion(jdbcDF, regions(i), spark)
      if (tmpdf != null) {
        //        LOG.info(tmpdf.schema)
        LOG.info(s"the region ID is : $regionID")
        LOG.info(s"saving result in :${Constant.RESULT_TABLE} \n")
        tmpdf.show(20)
        getStatus.saveResult(tmpdf, regionID, spark)
        LOG.info("==============")
        tmpdf.printSchema()
        LOG.info("saving  detail .....")
        val number_id = spark.read
          .format("jdbc")
          .option("url", Constant.DBURL + Constant.RESULTDB)
          .option("dbtable", Constant.RESULT_TABLE)
          .option("user", Constant.DBUSER)
          .option("password", Constant.DBPASSWD)
          .load().count()
        getStatus.saveResultDetail(tmpdf, number_id, spark)
      }

    }

    //106.6502,106.656237,26.651883,26.645094
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

