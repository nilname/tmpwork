package com.hx.bigdata

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.slf4j.LoggerFactory


///**
//  * Created by fangqing on 8/14/17.
//  * 这是主程序
//  */
object AppCore {
  val LOG = LoggerFactory.getLogger(AppCore.getClass);
  //初始化配置信息,读取外部配置文件
  Constant.init()

  def main(args: Array[String]): Unit = {
    //统计数据的起始时间对应Pos_time字段
    var start = "";
    //统计数据的结束时间对应Pos_time字段
    var end = "";
    //如果参数为0个,则表示默认从当前时间开始统计
    if (args.size == 0) {
      LOG.info(s"calculate interval is ${Constant.CALCULATE_INTERVAL}")
      start = getStatus.getLastNminute(Constant.CALCULATE_INTERVAL)
      end = getStatus.getLastNminute(0)
    }
    else {
        //从参数中读取起始时间和结束时间
      start = args(0)
      end = args(1)
    }
    //初始化spark环境
    val spark = SparkSession
      .builder()
      .config("spark.sql.shuffle.partitions", 20)
      .appName(Constant.APP_NAME)
      .getOrCreate()
    import spark.implicits._
    //获取出租车数据信息
    val jdbcDF1 = spark.read
      .format("jdbc")
      .option("url", Constant.DBURL + Constant.STAB_SOURCEDB + Constant.UTF8_STR)
      .option("dbtable", s"(select id,carno,company,pos_time,pos_lat,pos_lon,getpos_lat,getpos_lon,stoppos_lat,stoppos_lon,pos_angle,use_area,pay_amount,name from ${Constant.TAXIGPS_TABLE} where pos_time between  \'${start}\' and \'${end}\' ) as tmp_tb1 ")
      .option("user", Constant.DBUSER)
      .option("inferSchema", true)
      .option("password", Constant.DBPASSWD)
      .load()
    //获取出租车数据信息与上面相同,只是取数据的表不一样这个是k_h_taxigps
    val jdbcDF2 = spark.read
      .format("jdbc")
      .option("url", Constant.DBURL + Constant.STAB_SOURCEDB + Constant.UTF8_STR)
      .option("dbtable", s"(select id,carno,company,pos_time,pos_lat,pos_lon,getpos_lat,getpos_lon,stoppos_lat,stoppos_lon,pos_angle,use_area,pay_amount,name from ${Constant.STAB_TAXIGPS_TABLE} where pos_time between  \'${start}\' and \'${end}\' ) as tmp_tb2 ")
      .option("user", Constant.DBUSER)
      .option("inferSchema", true)
      .option("password", Constant.DBPASSWD)
      .load()
    //将上述两个数据合并成一个表
    val jdbcDF = jdbcDF1.union(jdbcDF2).withColumn("pos_lat", 'pos_lat.cast("Double")).withColumn("pos_lon", 'pos_lon.cast("Double")).cache()
    //打印日志输出获取到的记录数量
    println(s"jdbc count is ${jdbcDF.count()}")
    //获取道路和区域信息
    val regions = getStatus.getRegionInfo(spark)
    var tmpdf: DataFrame = null
    var regionID: Long = 0
    //一下循环计算每个区域中的出租次聚集情况
    for (i <- 0 until regions.size) {
      regionID = regions(i).select("bh").takeAsList(1).get(0).getString(0).toLong
      tmpdf = null
      //获取当前区域的类型是道路还是区域
      val flag = getStatus.getRegionType(regions(i)).trim()
      val trflag = flag.equals(Constant.REGION_FLAG.trim)
      //      LOG.info(s"==>this is $flag")
      //      LOG.info(s"==>this is ${Constant.REGION_FLAG.trim()}")
      //      LOG.info(s"==>this is ${trflag}")

      if (trflag) {
        LOG.info("this is in region")
//        计算区域内出租次聚集情况
        tmpdf = getStatus.getCarsfromRegion(jdbcDF, regions(i), spark).cache()
      }
      else {
        //计算道路上处组成聚集情况
        tmpdf = getStatus.getCarsfromroad(jdbcDF, regions(i), spark).cache()
        //        tmpdf = getStatus.getCarsfromroadML(jdbcDF, regions(i), spark)
      }

      if (tmpdf != null) {
        //保存本次计算结果到taxinumber
        getStatus.saveResult(tmpdf, regionID, spark)
        LOG.info("----------------->")
        tmpdf.printSchema()
        LOG.info(s"tmpdf count is ${tmpdf.count()}")
        tmpdf.show(false)
        val number_id = spark.read
          .format("jdbc")
          .option("url", Constant.DBURL + Constant.RESULTDB + Constant.UTF8_STR)
          .option("dbtable", Constant.RESULT_TABLE)
          .option("user", Constant.DBUSER)
          .option("password", Constant.DBPASSWD)
          .load().count()
        //
        //保存本次计算的明细表,如果被次统计该区域聚集数量为0则无明细信息
        getStatus.saveResultDetail(tmpdf, number_id, spark)
      }

    }


  }


}

