import sbt._

val mvnrepository = "MVN Repo" at "http://mvnrepository.com/artifact"

lazy val root = (project in file(".")).
  settings(

      organization := "valluvan",
      scalaVersion := "2.11.8",
      version := "1.0.0",

      ivyScala := ivyScala.value.map {
          _.copy(overrideScalaVersion = true)
      },
      name := "kConsumer",
      // https://mvnrepository.com/artifact/org.apache.spark/spark-core_2.11
      libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "2.1.1" % "provided",
      // https://mvnrepository.com/artifact/org.apache.spark/spark-streaming-kinesis-asl_2.11
      libraryDependencies += "org.apache.spark" % "spark-streaming-kinesis-asl_2.11" % "2.1.1",
      // https://mvnrepository.com/artifact/com.amazonaws/aws-java-sdk-core
      libraryDependencies += "com.amazonaws" % "aws-java-sdk-core" % "1.11.155",
      // https://mvnrepository.com/artifact/com.amazonaws/aws-java-sdk
      libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.11.155",
      // https://mvnrepository.com/artifact/com.amazonaws/amazon-kinesis-client
      libraryDependencies += "com.amazonaws" % "amazon-kinesis-client" % "1.7.6",


      libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.8.2",
      libraryDependencies += "org.apache.logging.log4j" % "log4j-api" % "2.8.2",

      resolvers ++= {
          Seq(mvnrepository)
      }
  )
