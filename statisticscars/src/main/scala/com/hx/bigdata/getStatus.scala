package com.hx.bigdata

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.regression.LinearRegression
//import org.
import org.apache.spark.sql.{DataFrame, functions, SparkSession}
import org.slf4j.LoggerFactory

/**
  * Created by fangqing on 8/14/17.
  */
object getStatus {

  val LOG = LoggerFactory.getLogger(getStatus.getClass);

  def getLastNminute(num: Int): String = {
    var dateFormat: SimpleDateFormat = new SimpleDateFormat(Constant.TIME_FORMATE)
    var cal: Calendar = Calendar.getInstance()
    cal.add(Calendar.MINUTE, -num)
    var yesterday = dateFormat.format(cal.getTime())
    yesterday
  }

  def getNminuteLater(num: Int): String = {
    var dateFormat: SimpleDateFormat = new SimpleDateFormat(Constant.TIME_FORMATE)
    var cal: Calendar = Calendar.getInstance()
    cal.add(Calendar.MINUTE, num)
    var yesterday = dateFormat.format(cal.getTime())
    yesterday
  }

  def getCarsfromRegion(cardf: DataFrame, regiondf: DataFrame, sparkSession: SparkSession): DataFrame = {
    import sparkSession.implicits._
    val tmpdf = regiondf.select("pos_lat", "pos_lon").agg(Map("pos_lon" -> "max", "pos_lat" -> "max")).takeAsList(1).get(0)
    val tmpdf_min = regiondf.select("pos_lat", "pos_lon").agg(Map("pos_lon" -> "min", "pos_lat" -> "min")).takeAsList(1).get(0)
    val a = tmpdf_min.getDouble(0)
    val b = tmpdf.getDouble(0)
    val c = tmpdf.getDouble(1)
    val d = tmpdf_min.getDouble(1)
    LOG.info(s"=>in region $a,$b,$c,$d")
    //    cardf.select("pos_lat","pos_lon").show(100,false)
    LOG.info("=>region")
    val middf = cardf.filter($"pos_lon" > a).filter($"pos_lon" < b).filter($"pos_lat" < c).filter($"pos_lat" > d)
    val final_df = middf.groupBy("carno").count().join(middf, "carno").filter("count >10")
    //    final_df.select("pos_lat","pos_lon").show(100,false)
    LOG.info("----------->region")
    final_df.printSchema()
    LOG.info(s"=>this result count is${final_df.count()}; orginal df is ${cardf.count()}")
    final_df
  }

  def getCarsfromroad(cardf: DataFrame, roaddf: DataFrame, sparkSession: SparkSession): DataFrame = {
    import sparkSession.implicits._
    val road_len = roaddf.count().toInt
    val tmplist = roaddf.select("pos_lat", "pos_lon").take(road_len)
    var a: Double = 0
    var b: Double = 0
    var c: Double = 0
    var d: Double = 0
    var middf: DataFrame = null
    var final_df: DataFrame = null
    LOG.info(s"=>in road $a,$b,$c,$d")
    //      cardf.select("pos_lat","pos_lon").show(100,false)
    LOG.info("=>road")
    for (i <- 0 until road_len) {
      a = tmplist(i).getDouble(0) + Constant.THRESHOLD
      b = tmplist(i).getDouble(0) - Constant.THRESHOLD
      c = tmplist(i).getDouble(1) + Constant.THRESHOLD
      d = tmplist(i).getDouble(1) - Constant.THRESHOLD
      //      LOG.info(s"=>in road $a,$b,$c,$d")
      //      cardf.select("pos_lat","pos_lon").show(100,false)
      //      LOG.info("=>road")
      middf = cardf.filter($"pos_lat" < a).filter($"pos_lat" > b).filter($"pos_lon" < c).filter($"pos_lon" > d)
      if (i == 0) {
        final_df = middf
      } else {
        final_df = final_df.union(middf).dropDuplicates()
      }
    }
    //    final_df.select("pos_lat","pos_lon").show(100,false)
    //    LOG.info("----------->road----------------")
    final_df
  }

  def getCarsfromroadML(cardf: DataFrame, roaddf: DataFrame, sparkSession: SparkSession): DataFrame = {
    import sparkSession.implicits._
    val tmpdf_max = roaddf.select("pos_lat", "pos_lon").agg(Map("pos_lon" -> "max", "pos_lat" -> "max")).takeAsList(1).get(0)
    val tmpdf_min = roaddf.select("pos_lat", "pos_lon").agg(Map("pos_lon" -> "min", "pos_lat" -> "min")).takeAsList(1).get(0)
    val a = tmpdf_min.getDouble(0)
    val b = tmpdf_max.getDouble(0)
    val c = tmpdf_max.getDouble(1)
    val d = tmpdf_min.getDouble(1)
    val testDF = cardf.filter($"pos_lon" > a).filter($"pos_lon" < b).filter($"pos_lat" < c).filter($"pos_lat" > d)
    val trainDF = roaddf.select("pos_lat", "pos_lon")

    //===============================
    val colArray = Array("pos_lat")
    val assembler = new VectorAssembler().setInputCols(colArray).setOutputCol("features")
    val vecDF: DataFrame = assembler.transform(trainDF)

    //===============================
    val colArray_for_predict = Array("pos_lat")
    val assembler_for_predict = new VectorAssembler().setInputCols(colArray_for_predict).setOutputCol("features")
    val vecDF_for_predict: DataFrame = assembler_for_predict.transform(trainDF)

    //=================================
    val lr1 = new LinearRegression()
    val lr2 = lr1.setFeaturesCol("features").setLabelCol("pos_lon").setFitIntercept(true).setStandardization(false)
    // RegParam：正则化
    val lr3 = lr2.setMaxIter(10).setRegParam(0.00003).setElasticNetParam(0.0001)
    val lr = lr3

    // 将训练集合代入模型进行训练
    val lrModel = lr.fit(vecDF)
    val predictions: DataFrame = lrModel.transform(vecDF_for_predict)
    println("输出预测结果")
    val predict_result: DataFrame = predictions.selectExpr("features", "pos_lat", "pos_lon", "prediction").withColumn("re",$"pos_lon"-$"prediction")
    LOG.info("正则化结果")
    predict_result.show(false)

    //    =========================
    val lr1x = new LinearRegression()
    val lr2x = lr1x.setFeaturesCol("features").setLabelCol("pos_lon").setFitIntercept(true)
    val lrx = lr2x
    // 将训练集合代入模型进行训练
    val lrModelx = lrx.fit(vecDF)
    val predictionsx: DataFrame = lrModelx.transform(vecDF_for_predict)
    println("输出预测结果")
    val predict_resultx: DataFrame = predictionsx.selectExpr("features", "pos_lat", "pos_lon", "prediction").withColumn("re",$"pos_lon"-$"prediction")
    LOG.info("没有正则化的结果")
    predict_resultx.show(false)
    predict_resultx


  }

  def getRegionInfo(sparkSession: SparkSession): Array[DataFrame] = {
    import sparkSession.implicits._
    val final_df0: DataFrame = sparkSession.read
      .format("jdbc")
      .option("url", Constant.DBURL + Constant.SOURCEDB + Constant.UTF8_STR)
      .option("dbtable", Constant.REGION_TABLE)
      .option("user", Constant.DBUSER)
      .option("inferSchema", true)
      .option("password", Constant.DBPASSWD)
      .load().cache()
    val final_df=final_df0.withColumn("pos_lat", 'pos_lat.cast("Double")).withColumn("pos_lon",'pos_lon.cast("Double"))
    val region_ids = final_df.select("bh").dropDuplicates()
    val region_num = region_ids.count().toInt
    val result_region = new Array[DataFrame](region_num)
    var region_index = ""
    for (i <- 0 until region_num) {
      region_index = region_ids.takeAsList(region_num).get(i).getString(0)
      result_region(i) = final_df.filter($"bh" === region_index).cache()
    }
    result_region
  }

  def getRegionType(df: DataFrame): String = {
    val region_type = df.select("leixin").takeAsList(1).get(0).getString(0)
    LOG.info("===================")
    LOG.info(region_type)
    LOG.info("===================")
    region_type
  }


  def saveResult(resultdf: DataFrame, regionID: Long, sparkSession: SparkSession): Unit = {
    import sparkSession.implicits._

    val now: Date = new Date()
    val dateFormat: SimpleDateFormat = new SimpleDateFormat(Constant.TIME_FORMATE)
    val compute_time = dateFormat.format(now)
    val car_numbers = resultdf.select("carno").dropDuplicates().count().toInt;
    //    val regionID = resultdf.select("bh").takeAsList(1).get(0).getInt(0)

    val tmp = results(0, regionID, compute_time, car_numbers)
    val res = Seq(tmp)
    val resDf = sparkSession.createDataFrame(res);
    resDf.write.mode("append")
      .format("jdbc")
      .option("url", Constant.DBURL + Constant.RESULTDB + Constant.UTF8_STR)
      .option("dbtable", Constant.RESULT_TABLE)
      .option("user", Constant.DBUSER)
      .option("password", Constant.DBPASSWD)
      .save()


  }


  def saveResultDetail(resultdf: DataFrame, number_id: Long, sparkSession: SparkSession): Unit = {
    import sparkSession.implicits._
    val tdf = resultdf.select("carno", "pos_time").groupBy("carno").agg(Map("pos_time" -> "max")).withColumnRenamed("max(pos_time)", "pos_time")
    val resDf = resultdf.withColumn("number_id", functions.lit(number_id)).select("number_id", "pos_time", "pos_lat", "pos_lon", "carno").dropDuplicates()
    resDf.join(tdf, Seq("carno", "pos_time")).dropDuplicates().withColumn("id", functions.lit(0)).write.mode("append")
      .format("jdbc")
      .option("url", Constant.DBURL + Constant.RESULTDB + Constant.UTF8_STR)
      .option("dbtable", Constant.DETAIL_RESULT_TABLE)
      .option("user", Constant.DBUSER)
      .option("password", Constant.DBPASSWD)
      .save()


  }

}

case class results(id: Long, id_bh: Long, compute_time: String, aggregated_quantity: Int)

case class resultsDetail(id: Long, number_id: Long, pos_lat: String, pos_lon: String, pos_time: String, carno: String)

//"id,number_id,pos_lat,pos_lon,pos_time,carno"

