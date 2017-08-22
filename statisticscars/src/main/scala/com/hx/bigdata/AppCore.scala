package com.hx.bigdata

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.slf4j.LoggerFactory


///**
//  * Created by fangqing on 8/14/17.
//  */
object AppCore {
  val LOG = LoggerFactory.getLogger(tmpTest.getClass);
  Constant.init()

  def main(args: Array[String]): Unit = {
    var start = "";
    var end = "";
    if (args.size == 0) {
      LOG.info(s"calculate interval is ${Constant.CALCULATE_INTERVAL}")
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


    val jdbcDF1 = spark.read
      .format("jdbc")
      .option("url", Constant.DBURL + Constant.RESULTDB + Constant.UTF8_STR)
      .option("dbtable", s"(select id,carno,company,pos_time,pos_lat,pos_lon,getpos_lat,getpos_lon,stoppos_lat,stoppos_lon,pos_angle,use_area,pay_amount,name from ${Constant.TAXIGPS_TABLE} where pos_time between  \'${start}\' and \'${end}\' ) as tmp_tb1 ")
      .option("user", Constant.DBUSER)
      .option("password", Constant.DBPASSWD)
      .load()

    val jdbcDF2 = spark.read
      .format("jdbc")
      .option("url", Constant.DBURL + Constant.STAB_SOURCEDB + Constant.UTF8_STR)
      .option("dbtable", s"(select id,carno,company,pos_time,pos_lat,pos_lon,getpos_lat,getpos_lon,stoppos_lat,stoppos_lon,pos_angle,use_area,pay_amount,name from ${Constant.STAB_TAXIGPS_TABLE} where pos_time between  \'${start}\' and \'${end}\' ) as tmp_tb2 ")
      .option("user", Constant.DBUSER)
      .option("password", Constant.DBPASSWD)
      .load()


    val jdbcDF = jdbcDF1.union(jdbcDF2)
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
          .option("url", Constant.DBURL + Constant.RESULTDB + Constant.UTF8_STR)
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


/*




"id,carno,company,pos_time,pos_lat,pos_lon,getpos_lat,getpos_lon,stoppos_lat,stoppos_lon,pos_angle,use_area,pay_amount,name"



+-------------+--------------+------+-----+---------+-------+
| Field       | Type         | Null | Key | Default | Extra |
+-------------+--------------+------+-----+---------+-------+
| id          | varchar(200) | NO   | PRI | NULL    |       |
| carno       | varchar(20)  | YES  |     | NULL    |       |
| company     | varchar(50)  | YES  |     | NULL    |       |
| pos_time    | datetime     | YES  |     | NULL    |       |
| pos_lat     | varchar(50)  | YES  |     | NULL    |       |
| pos_lon     | varchar(50)  | YES  |     | NULL    |       |
| getpos_lat  | varchar(50)  | YES  |     | NULL    |       |
| getpos_lon  | varchar(50)  | YES  |     | NULL    |       |
| stoppos_lat | varchar(50)  | YES  |     | NULL    |       |
| stoppos_lon | varchar(50)  | YES  |     | NULL    |       |
| pos_angle   | varchar(50)  | YES  |     | NULL    |       |
| use_area    | varchar(100) | YES  |     | NULL    |       |
| pay_amount  | varchar(50)  | YES  |     | NULL    |       |
| name        | varchar(20)  | YES  |     | NULL    |       |
| idcard      | varchar(20)  | YES  |     | NULL    |       |
| sex         | varchar(4)   | YES  |     | NULL    |       |
| telephone   | varchar(50)  | YES  |     | NULL    |       |
| adress      | varchar(100) | YES  |     | NULL    |       |
| plate_time  | datetime     | YES  |     | NULL    |       |
| bz          | varchar(50)  | YES  |     | NULL    |       |
| flag        | int(2)       | NO   |     | 1       |       |
+-------------+--------------+------+-----+---------+-------+


+-------------+--------------+------+-----+---------+-------+
| Field       | Type         | Null | Key | Default | Extra |
+-------------+--------------+------+-----+---------+-------+
| id          | varchar(200) | NO   | PRI | NULL    |       |
| carno       | varchar(20)  | YES  |     | NULL    |       |
| company     | varchar(50)  | YES  |     | NULL    |       |
| pos_time    | datetime     | YES  |     | NULL    |       |
| pos_lat     | varchar(50)  | YES  |     | NULL    |       |
| pos_lon     | varchar(50)  | YES  |     | NULL    |       |
| getpos_lat  | varchar(50)  | YES  |     | NULL    |       |
| getpos_lon  | varchar(50)  | YES  |     | NULL    |       |
| stoppos_lat | varchar(50)  | YES  |     | NULL    |       |
| stoppos_lon | varchar(50)  | YES  |     | NULL    |       |
| pos_angle   | varchar(50)  | YES  |     | NULL    |       |
| use_area    | varchar(100) | YES  |     | NULL    |       |
| pay_amount  | varchar(50)  | YES  |     | NULL    |       |
| name        | varchar(20)  | YES  |     | NULL    |       |
| idcard      | varchar(20)  | YES  |     | NULL    |       |
| sex         | varchar(4)   | YES  |     | NULL    |       |
| telephone   | varchar(50)  | YES  |     | NULL    |       |
| address     | varchar(100) | YES  |     | NULL    |       |
| plate_time  | datetime     | YES  |     | NULL    |       |
| bz          | varchar(50)  | YES  |     | NULL    |       |
+-------------+--------------+------+-----+---------+-------+

 */
//case class results(id: Long, compute_time: String, aggregated_quantity: Int)

