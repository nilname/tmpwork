while true
do
#spark-submit  --class  com.hx.bigdata.AppCore  --master local[32]  statisticscars-1.0-SNAPSHOT-jar-with-dependencies.jar "2017-07-16 21:44:00" "2017-07-16 21:44:33" &>run.log
spark-submit  --class  com.hx.bigdata.AppCore  --master yarn  --executor-memory 20G  --num-executors 20  statisticscars-1.0-SNAPSHOT-jar-with-dependencies.jar  &>run.log
sleep 1800
done
