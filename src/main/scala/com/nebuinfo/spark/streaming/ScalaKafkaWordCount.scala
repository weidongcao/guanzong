package com.nebuinfo.spark.streaming

import com.nebuinfo.conf.ConfigurationManager
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Demo
  * Spark Streaming远程连接Zookeeper读取Kafka的数据进行单词统计
  * Created by CaoWeiDong on 2017-11-22.
  */
object ScalaKafkaWordCount {
    def main(args: Array[String]): Unit = {

        //Zookeeper连接地址
        val zkQuorum = ConfigurationManager.getProperty("zkQuorum")

        //Kafka连接信息(Topic Partition)
        val topicMap = Map("test" -> 1)

        //conf
        val conf = new SparkConf()
            .setAppName(ScalaKafkaWordCount.getClass.getSimpleName)
            .setMaster("local[4]")

        //SparkStreaming
        val ssc = new StreamingContext(conf, Seconds(2))

        //SparkStreaming连接Kafka
        val lines = KafkaUtils.createStream(ssc, zkQuorum, "sss", topicMap)
            .map(_._2)

        //以空格进行切分，统计单词个数
        val words = lines.flatMap(_.split(" "))
        val wordCounts = words.map(key => (key, 1L))
            .reduceByKey(_ + _)

        //打印
        wordCounts.print()

        //启动
        ssc.start()
        ssc.awaitTermination()
    }
}
