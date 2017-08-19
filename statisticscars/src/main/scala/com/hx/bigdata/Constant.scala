package com.hx.bigdata

/**
  * Created by fangqing on 8/18/17.
  */
object Constant {
  val SOURCEDB="sjww"
  val RESULTDB="test12"
  val DBURL = "jdbc:mysql://10.10.60.196:3306/"
  val DBUSER = "zfw"
  val DBPASSWD = "123456"
  val REGION_TABLE = "taxi_region"
  val TAXIGPS_TABLE = s"taxigps"
  val RESULT_TABLE = "test_taxinumber"
  val DETAIL_RESULT_TABLE = "taxidetail"
  val APP_NAME = "car statistics"
  val CALCULATE_INTERVAL = 15
  val TIME_FORMATE = "yyyy-MM-dd HH:mm:ss"
  val TAXIGPS_TABLE_FEILDS = "id,carno,company,pos_time,pos_lat,pos_lon,getpos_lat,getpos_lon,stoppos_lat,stoppos_lon," +
                             "pos_angle,use_area,pay_amount,name,idcard,sex,telephone,address,plate_time,bz"
  val RESULT_TABLE_FIELDS = "id,id_bh,compute_time,aggregated_quantity"
  val REGION_TABLE_FIELDS = "id,bh,region_type,regin,pos_lon,pos_lat"
  val DETAIL_RESULT_TABLE_FIELDS = "id,number_id,pos_lat,pos_lon,pos_time,carno"
  val REGION_FLAG="区域"


}


/*

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

taxinumber
create table taxinumber(id int not null auto_increment primary key, compute_time timestamp,aggregated_quantity int)

create table taxinumber(id int not null auto_increment primary key, id_bh int,compute_time timestamp,aggregated_quantity int)
create table test_taxinumber(id int not null auto_increment primary key, id_bh int,compute_time timestamp,aggregated_quantity int)

taxidetail
create table taxidetail(id int not null auto_increment primary key, number_id int,pos_lat varchar(50),pos_lon varchar(50),pos_time timestamp,carno varchar(20))

 val REGION_TABLE_FIELDS = "id,bh,regin,pos_lon,pos_lat"

taxidetail
create table taxi_region(id int not null auto_increment primary key, bh int,regin varchar(50),pos_lon varchar(50),pos_lat varchar(50))



+---------+-------------+------+-----+---------+-------+
| Field   | Type        | Null | Key | Default | Extra |
+---------+-------------+------+-----+---------+-------+
| id      | varchar(50) | NO   | PRI |         |       |
| bh      | varchar(20) | NO   |     | NULL    |       |
| regin   | varchar(50) | YES  |     | NULL    |       |
| pos_lon | double      | YES  |     | NULL    |       |
| pos_lat | double      | YES  |     | NULL    |       |
| leixin  | varchar(20) | YES  |     | NULL    |       |
+---------+-------------+------+-----+---------+-------+

 */