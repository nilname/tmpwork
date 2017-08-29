package com.hx.bigdata

/**
  * Created by fangqing on 8/22/17.
  */

import org.apache.spark.mllib.regression.LinearRegressionWithSGD
import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}


object RegressionMetricsExample {

  def main(args: Array[String]) : Unit = {
    //information set
    val conf = new SparkConf().setAppName("RegressionMetricsExample").setMaster("local")//.setMaster("local")为本地运行程序
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    // Load the data
    //载入数据  data(label,[feature1,feature2,...])
    val data = MLUtils.loadLibSVMFile(sc, "F:/HDFSinputfile/sample_linear_regression_data.txt").cache()//Data 为 RDD

    // Build the model
    val numIterations = 100
    val model = LinearRegressionWithSGD.train(data, numIterations)//SGD:stochastic gradient descent  线性回归

    // Get predictions
    val valuesAndPreds = data.map{ point =>
      val prediction = model.predict(point.features)
      (prediction, point.label)//(预测值，实际值)
    }//返回的是一个RDD数据类型

    println(valuesAndPreds.getClass)//class org.apache.spark.rdd.MapPartitionsRDD  RDD数据类型

    println("value and predict")
    //valuesAndPreds.foreach(println)//返回的是(1.1470019382890901,-9.490009878824548) (-0.5402104097029286,0.2577820163584905)

    // Instantiate metrics object
    val metrics = new RegressionMetrics(valuesAndPreds)

    // Squared error
    println("Model Parameter")
    var i=1
    model.weights.toArray.foreach(
      a=> {
        println("Parameter" + i + ":" + a)
        i+=1
      }
    )
    println("model intercept:"+model.intercept)


    println("+MSE = "+metrics.meanSquaredError)//${metrics.meanSquaredError} println中打印变量 平均平方误差
    println(s"RMSE = ${metrics.rootMeanSquaredError}")//另外一种打印字符与程序中变量值方式  标准平均平法误差

    // R-squared
    println(s"R-squared = ${metrics.r2}")

    // Mean absolute error
    println(s"MAE = ${metrics.meanAbsoluteError}")

    // Explained variance
    println(s"Explained variance = ${metrics.explainedVariance}")
    sc.stop()

  }


}
