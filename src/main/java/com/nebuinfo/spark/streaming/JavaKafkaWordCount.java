package com.nebuinfo.spark.streaming;

import com.nebuinfo.conf.ConfigurationManager;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by CaoWeiDong on 2017-11-21.
 */
public final class JavaKafkaWordCount {
    private static final Logger logger = LoggerFactory.getLogger(JavaKafkaWordCount.class);
    //设置匹配模式，以空格分隔
    private static final Pattern SPACE = Pattern.compile(" ");

    private JavaKafkaWordCount() {

    }

    public static void main(String[] args) throws InterruptedException {
        //Zookeeper的地址
        String zkHost = ConfigurationManager.getProperty("zkHost");
        String[] topics = new String[]{"dong"};
        int numThreads = 1;
        Map<String, Integer> topicMap = new HashMap<>();
        for (String topic : topics) {
            topicMap.put(topic, numThreads);
        }

        SparkConf conf = new SparkConf()
                .setAppName(JavaKafkaWordCount.class.getSimpleName())
                .setMaster("local");
        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(5));

        JavaPairReceiverInputDStream<String, String> messages =
                KafkaUtils.createStream(jssc, zkHost, "dong", topicMap);

        JavaDStream<String> linesDS = messages.map(
                (Function<Tuple2<String, String>, String>) tuple2 -> tuple2._2()
        );

        JavaDStream<String> wordsDS = linesDS.flatMap(
                (FlatMapFunction<String, String>) line -> Arrays.asList(SPACE.split(line)).iterator()
        );

        JavaPairDStream<String, Integer> wordCountDS = wordsDS.mapToPair(
                (PairFunction<String, String, Integer>) key -> new Tuple2<>(key, 1)
        ).reduceByKey(
                (Function2<Integer, Integer, Integer>) (integer, integer2) -> integer + integer2
        );

        wordCountDS.print();
        jssc.start();
        jssc.awaitTermination();
    }
}
