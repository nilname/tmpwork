package com.hx.bigdata
/**
  * hxfeng
  * 利用线性回归模型对数据进行回归预测
  */


import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.regression.LinearRegression
object linnear_regression_online {
  def main(args: Array[String]): Unit = {

    //设置环境
    val conf=new SparkConf().setAppName("tianchi").setMaster("local")
    val sc=new SparkContext(conf)
    val sqc=new SQLContext(sc)

    //准备训练集合
    val raw_data=sc.textFile("/home/wangtuntun/LR_data")
    val map_data=raw_data.map{x=>
      val split_list=x.split(",")
      (split_list(0).toDouble,split_list(1).toDouble,split_list(2).toDouble,split_list(3).toDouble,split_list(4).toDouble,split_list(5).toDouble,split_list(6).toDouble,split_list(7).toDouble)
    }
    val df=sqc.createDataFrame(map_data)
    val data = df.toDF("Population", "Income", "Illiteracy", "LifeExp", "Murder", "HSGrad", "Frost", "Area")
    val colArray = Array("Population", "Income", "Illiteracy", "LifeExp", "HSGrad", "Frost", "Area")
    val assembler = new VectorAssembler().setInputCols(colArray).setOutputCol("features")
    val vecDF: DataFrame = assembler.transform(data)

    //准备预测集合
    val raw_data_predict=sc.textFile("/home/wangtuntun/LR_data_for_predict")
    val map_data_for_predict=raw_data_predict.map{x=>
      val split_list=x.split(",")
      (split_list(0).toDouble,split_list(1).toDouble,split_list(2).toDouble,split_list(3).toDouble,split_list(4).toDouble,split_list(5).toDouble,split_list(6).toDouble,split_list(7).toDouble)
    }
    val df_for_predict=sqc.createDataFrame(map_data_for_predict)
    val data_for_predict = df_for_predict.toDF("Population", "Income", "Illiteracy", "LifeExp", "Murder", "HSGrad", "Frost", "Area")
    val colArray_for_predict = Array("Population", "Income", "Illiteracy", "LifeExp", "HSGrad", "Frost", "Area")
    val assembler_for_predict = new VectorAssembler().setInputCols(colArray_for_predict).setOutputCol("features")
    val vecDF_for_predict: DataFrame = assembler_for_predict.transform(data_for_predict)

    // 建立模型，预测谋杀率Murder
    // 设置线性回归参数
    val lr1 = new LinearRegression()
    val lr2 = lr1.setFeaturesCol("features").setLabelCol("Murder").setFitIntercept(true)
    // RegParam：正则化
    val lr3 = lr2.setMaxIter(10).setRegParam(0.3).setElasticNetParam(0.8)
    val lr = lr3

    // 将训练集合代入模型进行训练
    val lrModel = lr.fit(vecDF)

    // 输出模型全部参数
    lrModel.extractParamMap()
    // Print the coefficients and intercept for linear regression
    println(s"Coefficients: ${lrModel.coefficients} Intercept: ${lrModel.intercept}")

    // 模型进行评价
    val trainingSummary = lrModel.summary
    println(s"numIterations: ${trainingSummary.totalIterations}")
    println(s"objectiveHistory: ${trainingSummary.objectiveHistory.toList}")
    trainingSummary.residuals.show()
    println(s"RMSE: ${trainingSummary.rootMeanSquaredError}")
    println(s"r2: ${trainingSummary.r2}")


    val predictions: DataFrame = lrModel.transform(vecDF_for_predict)
    //    val predictions = lrModel.transform(vecDF)
    println("输出预测结果")
    val predict_result: DataFrame =predictions.selectExpr("features","Murder", "round(prediction,1) as prediction")
    predict_result.foreach(println(_))
    sc.stop()
  }
}


/*

3615, 3624, 2.1, 69.05, 15.1, 41.3, 20, 50708
365, 6315, 1.5, 69.31, 11.3, 66.7, 152, 566432
2212, 4530, 1.8, 70.55, 7.8, 58.1, 15, 113417
2110, 3378, 1.9, 70.66, 10.1, 39.9, 65, 51945
21198, 5114, 1.1, 71.71, 10.3, 62.6, 20, 156361
2541, 4884, 0.7, 72.06, 6.8, 63.9, 166, 103766
3100, 5348, 1.1, 72.48, 3.1, 56, 139, 4862
579, 4809, 0.9, 70.06, 6.2, 54.6, 103, 1982
8277, 4815, 1.3, 70.66, 10.7, 52.6, 11, 54090
4931, 4091, 2, 68.54, 13.9, 40.6, 60, 58073
868, 4963, 1.9, 73.6, 6.2, 61.9, 0, 6425
813, 4119, 0.6, 71.87, 5.3, 59.5, 126, 82677
11197, 5107, 0.9, 70.14, 10.3, 52.6, 127, 55748
5313, 4458, 0.7, 70.88, 7.1, 52.9, 122, 36097
2861, 4628, 0.5, 72.56, 2.3, 59, 140, 55941
2280, 4669, 0.6, 72.58, 4.5, 59.9, 114, 81787
3387, 3712, 1.6, 70.1, 10.6, 38.5, 95, 39650
3806, 3545, 2.8, 68.76, 13.2, 42.2, 12, 44930
1058, 3694, 0.7, 70.39, 2.7, 54.7, 161, 30920
4122, 5299, 0.9, 70.22, 8.5, 52.3, 101, 9891
5814, 4755, 1.1, 71.83, 3.3, 58.5, 103, 7826
9111, 4751, 0.9, 70.63, 11.1, 52.8, 125, 56817
3921, 4675, 0.6, 72.96, 2.3, 57.6, 160, 79289
2341, 3098, 2.4, 68.09, 12.5, 41, 50, 47296
4767, 4254, 0.8, 70.69, 9.3, 48.8, 108, 68995
746, 4347, 0.6, 70.56, 5, 59.2, 155, 145587
1544, 4508, 0.6, 72.6, 2.9, 59.3, 139, 76483
590, 5149, 0.5, 69.03, 11.5, 65.2, 188, 109889
812, 4281, 0.7, 71.23, 3.3, 57.6, 174, 9027
7333, 5237, 1.1, 70.93, 5.2, 52.5, 115, 7521
1144, 3601, 2.2, 70.32, 9.7, 55.2, 120, 121412
18076, 4903, 1.4, 70.55, 10.9, 52.7, 82, 47831
5441, 3875, 1.8, 69.21, 11.1, 38.5, 80, 48798
637, 5087, 0.8, 72.78, 1.4, 50.3, 186, 69273
10735, 4561, 0.8, 70.82, 7.4, 53.2, 124, 40975
2715, 3983, 1.1, 71.42, 6.4, 51.6, 82, 68782
2284, 4660, 0.6, 72.13, 4.2, 60, 44, 96184
11860, 4449, 1, 70.43, 6.1, 50.2, 126, 44966
931, 4558, 1.3, 71.9, 2.4, 46.4, 127, 1049
2816, 3635, 2.3, 67.96, 11.6, 37.8, 65, 30225
681, 4167, 0.5, 72.08, 1.7, 53.3, 172, 75955
4173, 3821, 1.7, 70.11, 11, 41.8, 70, 41328
12237, 4188, 2.2, 70.9, 12.2, 47.4, 35, 262134
1203, 4022, 0.6, 72.9, 4.5, 67.3, 137, 82096
472, 3907, 0.6, 71.64, 5.5, 57.1, 168, 9267
4981, 4701, 1.4, 70.08, 9.5, 47.8, 85, 39780
3559, 4864, 0.6, 71.72, 4.3, 63.5, 32, 66570
1799, 3617, 1.4, 69.48, 6.7, 41.6, 100, 24070
4589, 4468, 0.7, 72.48, 3, 54.5, 149, 54464
376, 4566, 0.6, 70.29, 6.9, 62.9, 173, 97203
 */

/*


615, 3624, 2.1, 69.05, 0, 41.3, 20, 50708
65, 6315, 1.5, 69.31, 0, 66.7, 152, 566432
212, 4530, 1.8, 70.55, 0, 58.1, 15, 113417
110, 3378, 1.9, 70.66, 0, 39.9, 65, 51945
1198, 5114, 1.1, 71.71,0, 62.6, 20, 156361
541, 4884, 0.7, 72.06, 0, 63.9, 166, 103766
100, 5348, 1.1, 72.48, 0, 56, 139, 4862
79, 4809, 0.9, 70.06, 0, 54.6, 103, 1982
277, 4815, 1.3, 70.66, 0, 52.6, 11, 54090
931, 4091, 2, 68.54, 0, 40.6, 60, 58073
868, 4963, 1.9, 73.6, 6.2, 61.9, 0, 6425
813, 4119, 0.6, 71.87, 5.3, 59.5, 126, 82677
11197, 5107, 0.9, 70.14, 10.3, 52.6, 127, 55748
5313, 4458, 0.7, 70.88, 7.1, 52.9, 122, 36097
2861, 4628, 0.5, 72.56, 2.3, 59, 140, 55941
2280, 4669, 0.6, 72.58, 4.5, 59.9, 114, 81787
3387, 3712, 1.6, 70.1, 10.6, 38.5, 95, 39650
3806, 3545, 2.8, 68.76, 13.2, 42.2, 12, 44930
1058, 3694, 0.7, 70.39, 2.7, 54.7, 161, 30920
4122, 5299, 0.9, 70.22, 8.5, 52.3, 101, 9891
5814, 4755, 1.1, 71.83, 3.3, 58.5, 103, 7826
9111, 4751, 0.9, 70.63, 11.1, 52.8, 125, 56817
3921, 4675, 0.6, 72.96, 2.3, 57.6, 160, 79289
2341, 3098, 2.4, 68.09, 12.5, 41, 50, 47296
4767, 4254, 0.8, 70.69, 9.3, 48.8, 108, 68995
746, 4347, 0.6, 70.56, 5, 59.2, 155, 145587
1544, 4508, 0.6, 72.6, 2.9, 59.3, 139, 76483
590, 5149, 0.5, 69.03, 11.5, 65.2, 188, 109889
812, 4281, 0.7, 71.23, 3.3, 57.6, 174, 9027
7333, 5237, 1.1, 70.93, 5.2, 52.5, 115, 7521
1144, 3601, 2.2, 70.32, 9.7, 55.2, 120, 121412
18076, 4903, 1.4, 70.55, 10.9, 52.7, 82, 47831
5441, 3875, 1.8, 69.21, 11.1, 38.5, 80, 48798
637, 5087, 0.8, 72.78, 1.4, 50.3, 186, 69273
10735, 4561, 0.8, 70.82, 7.4, 53.2, 124, 40975
2715, 3983, 1.1, 71.42, 6.4, 51.6, 82, 68782
2284, 4660, 0.6, 72.13, 4.2, 60, 44, 96184
11860, 4449, 1, 70.43, 6.1, 50.2, 126, 44966
931, 4558, 1.3, 71.9, 2.4, 46.4, 127, 1049
2816, 3635, 2.3, 67.96, 11.6, 37.8, 65, 30225
681, 4167, 0.5, 72.08, 1.7, 53.3, 172, 75955
4173, 3821, 1.7, 70.11, 11, 41.8, 70, 41328
12237, 4188, 2.2, 70.9, 12.2, 47.4, 35, 262134
1203, 4022, 0.6, 72.9, 4.5, 67.3, 137, 82096
472, 3907, 0.6, 71.64, 5.5, 57.1, 168, 9267
4981, 4701, 1.4, 70.08, 9.5, 47.8, 85, 39780
3559, 4864, 0.6, 71.72, 4.3, 63.5, 32, 66570
1799, 3617, 1.4, 69.48, 6.7, 41.6, 100, 24070
4589, 4468, 0.7, 72.48, 3, 54.5, 149, 54464
376, 4566, 0.6, 70.29, 6.9, 62.9, 173, 97203
 */