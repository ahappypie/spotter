name := "spotter"

version := "0.1"

scalaVersion := "2.12.10"

lazy val sparkVersion = "2.4.4"
lazy val hadoopVersion = "2.10.0"

resolvers += "confluent" at "https://packages.confluent.io/maven"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion % "provided",
  "za.co.absa" %% "abris" % "3.1.1",
  "org.apache.hadoop" % "hadoop-common" % hadoopVersion % "provided",
  "org.apache.hadoop" % "hadoop-aws" % hadoopVersion % "provided",
  "com.amazonaws" % "aws-java-sdk-bundle" % "1.11.271" % "provided",
  "org.apache.hudi" %% "hudi-spark-bundle" % "0.5.1-incubating"
)

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.7"
dependencyOverrides += "org.apache.avro" % "avro" % "1.8.2" //needed to keep abris on 1.8.x instead of 1.9.x