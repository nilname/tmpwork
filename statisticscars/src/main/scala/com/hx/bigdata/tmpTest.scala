package com.hx.bigdata

import java.io.{File, FileInputStream}
import java.util.Properties

import org.slf4j.Logger
import org.slf4j.LoggerFactory;

/**
  * Created by fangqing on 8/19/17.
  */
object tmpTest {
  //  def init():Unit={
  //    val filepath=new File(System.getProperty("user.dir"))
  //    val filedir=filepath.getParent+File.separator+"statistics.properties"
  //    this.prop.load(new FileInputStream(filedir))
  //    this.RESULTDB=prop.getProperty("DB")
  //  }
  //  val prop=new Properties()
  //  val SOURCEDB
  //  val RESULTDB = "test12"

  def main(args: Array[String]): Unit = {
    val LOG = LoggerFactory.getLogger(tmpTest.getClass);
    if ("区域".eq("区域")) {
      tmpConstant.init()
      System.out.println(tmpGet.getx())
      LOG.info("this is info")
      LOG.error("this is error")
    }
    //   System.out.print("hello")
  }

}
