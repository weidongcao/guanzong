package com.nebuinfo.spark.streaming

import com.nebuinfo.conf.ConfigurationManager
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Created by CaoWeiDong on 2017-11-22.
  */
object ScalaKafkaWordCount {
    def main(args: Array[String]): Unit ={

        val zkQuorum = ConfigurationManager.getProperty("zkQuorum")

        val topicMap = Map("dong" -> 1)

        val conf = new SparkConf()
            .setAppName(ScalaKafkaWordCount.getClass.getSimpleName)
            .setMaster("local")

        val ssc = new StreamingContext(conf, Seconds(2))

        val lines = KafkaUtils.createStream(ssc, zkQuorum, "sss", topicMap)
            .map(_._2)

        val words = lines.flatMap(_.split(" "))
        val wordCounts = words.map(key => (key, 1L))
            .reduceByKey(_ + _)

        wordCounts.print()

        ssc.start()
        ssc.awaitTermination()
    }

}
