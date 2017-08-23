package valluvan

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream
import valluvan.utils.Logger
import java.io._

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.kinesis.KinesisUtils
import org.apache.spark.streaming.{Milliseconds, StreamingContext}

import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.ShutdownReason

object Consumer {
    def main(args: Array[String]): Unit = {
        val logger = Logger.getInstance(getClass.getSimpleName)

        // === Configurations for Kinesis streams ===
        val kinesisStreamName = "quickstats-development-selva"
        val kinesisEndpointUrl = "https://kinesis.us-west-2.amazonaws.com" // e.g. https://kinesis.us-west-2.amazonaws.com"
        val kinesisRegion = "us-west-2"
        val s3BucketName = "quickstats-development-selva"

        val awsCredentialsProvider = new DefaultAWSCredentialsProviderChain()
        val awsCredentials = awsCredentialsProvider.getCredentials()

        // Create the low-level Kinesis Client from the AWS Java SDK.
        val kinesisClient = AmazonKinesisClientBuilder
          .standard()
          .withEndpointConfiguration(new EndpointConfiguration(kinesisEndpointUrl, kinesisRegion))
          .withCredentials(awsCredentialsProvider)
          .build()

        logger.info(kinesisClient.describeStream(kinesisStreamName))

        // Spark Streaming batch interval
        val batchInterval = Milliseconds(2000)
        val kinesisCheckpointInterval = batchInterval

        // Setup the SparkConfig and StreamingContext
        val sparkConfig = new SparkConf().setAppName("quickstats-development-selva").setMaster("local[4]")
        val ssc = new StreamingContext(sparkConfig, batchInterval)

        val kinesisStreams = KinesisUtils.createStream(ssc, "quickstats-development-selva", kinesisStreamName, kinesisEndpointUrl, kinesisRegion,
                InitialPositionInStream.LATEST, kinesisCheckpointInterval, StorageLevel.MEMORY_AND_DISK_2)

        val streamingData = kinesisStreams.map(byteArray => {
            new String(byteArray)
        })

        streamingData.foreachRDD({ rdd =>
            if (!rdd.isEmpty()) {
                val file = new FileWriter("./log/sample.txt", true)
                logger.info("Records in this batch: " + rdd.count())
                logger.info("First Record: " + rdd.first())
//                Thread.sleep(5000)
                logger.info("Last Record: " + rdd.collect().last)
                rdd.collect().foreach(data => {
                    file.write(data + "\n")
                })
                file.close()
            }
        })

        // Start the streaming context and await termination


        val mainThread = Thread.currentThread
        Runtime.getRuntime.addShutdownHook(new Thread() {override def run = {
            logger.info("HERE")
            mainThread.join()
        }})

        ssc.start()
        ssc.awaitTermination()


    }




//    def main(args: Array[String]) {
//        var keepRunning = true
//
//        val sparkConfig = new SparkConf().setAppName("quickstats-development-selva").setMaster("local[4]")
//        val ssc = new StreamingContext(sparkConfig, Milliseconds(1000))
//        val lines = ssc.socketTextStream("localhost", 9999)
//        val words = lines.flatMap(_.split(" "))
//        val pairs = words.map(word => (word, 1))
//
//        val wordCounts = pairs.reduceByKey(_ + _)
//
//        val mainThread = Thread.currentThread()
//        Runtime.getRuntime.addShutdownHook(new Thread() {override def run = {
//            println("inside addShutDownHook handler")
//            keepRunning = false
//            ssc.stop(stopSparkContext = true, stopGracefully = true)
//            mainThread.join()
//        }})
//
//        // Print the first ten elements of each RDD generated in this DStream to the console
//        wordCounts.print()
//        ssc.start()             // Start the computation
//        ssc.awaitTermination()
////        while (keepRunning) {
////            println("in while loop")
////            Thread.sleep(1000)
////        }
//        // Graceful shutdown code goes here that needs to be on the main thread
//        println("Exiting main")
//    }
}
