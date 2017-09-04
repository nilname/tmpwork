package com.hx.bigdata

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

/**
  * Created by fangqing on 8/22/17.
  */
object tmpConstant {
  def main(args: Array[String]): Unit = {
    val tmpArray=Array(1,2,3,4,5)
    val tolist=tmpArray.to[ListBuffer]//将tmpArray专成listBuffer
    tmpArray.update(1,3)//修改第一个元素为3
    tmpArray.apply(3)//获得第三个元素的值同tmpArray(3)
    tmpArray.length//获得数组的长度
    val copytmpArray=tmpArray.clone()//克隆一个数组
    //TODO
    tmpArray.canEqual(3)//不理解
    tmpArray.size//获取数组大小 与length相同
    tmpArray.take(2)//获取前两个
    tmpArray.to[ArrayBuffer]//转换为ArrayBuffer
    tmpArray.zipWithIndex//与索引合并
    tmpArray.view
    tmpArray.updated(1,8)//更新数组的元素返回新的数组原来的数组不变
    tmpArray.transform(x=>x*4)//转换修改了原来的数组
    tmpArray.span(x=>x<30)
    tmpArray.combinations(2)//排列组合
    tmpArray.runWith(x=>x<30)(32)//具体不理解含义是什么
//    tmpArray.


    val tmpList=List(1,2,3,4)
    tmpList
    val tmpMap=Map("a"->1,"b"->2)
    tmpMap
    val  tmpSet=Set(1,2,3,3,3,4,5,6,4)
    tmpSet
    val tmptuple=Tuple4(1,2,3,4)
    tmptuple

  }
}
