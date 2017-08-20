package com.hx.bigdata

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
  * Created by fangqing on 8/19/17.
  */
object tmpTest {

  def main(args: Array[String]): Unit = {
    val  LOG = LoggerFactory.getLogger(tmpTest.getClass);
    if("区域".eq("区域"))
      {
        System.out.println("okay")
        LOG.info("this is info")
        LOG.error("this is error")
      }
//   System.out.print("hello")
  }

}
