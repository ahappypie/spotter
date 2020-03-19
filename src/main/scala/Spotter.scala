import java.time.Instant

object Spotter extends SparkSessionWrapper {
  def main(args: Array[String]): Unit = {
    val startTime = Instant.now()
    val schemaRegistryURL = "http://localhost:8081"
    val topic = "spotter"

    val df = spark.read
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribePattern", topic)
      .option("startingOffsets", "earliest") // From starting
      .load()

    import za.co.absa.abris.avro.functions.from_confluent_avro
    import za.co.absa.abris.avro.read.confluent.SchemaManager
    import spark.implicits._

    val schemaRegistryConfig = Map(
      SchemaManager.PARAM_SCHEMA_REGISTRY_URL          -> schemaRegistryURL,
      SchemaManager.PARAM_SCHEMA_REGISTRY_TOPIC        -> topic,
      SchemaManager.PARAM_VALUE_SCHEMA_NAMING_STRATEGY -> SchemaManager.SchemaStorageNamingStrategies.TOPIC_NAME, // choose a subject name strategy
      SchemaManager.PARAM_VALUE_SCHEMA_ID              -> "latest" // set to "latest" if you want the latest schema version to used
    )

    val data = df.select(from_confluent_avro(df.col("value"), schemaRegistryConfig) as 'data).select("data.*")
    data.printSchema
    println(s"number of records = ${data.count}")
//
//    import org.apache.spark.sql.functions.{min, avg, max, stddev, round}
//
//    data.filter($"instance" like "%dn%")
//      .groupBy($"instance")
//      .agg(
//        min($"price"),
//        round(avg($"price"), 4),
//        max($"price"),
//        round(stddev($"price"), 4),
//        round((avg($"price") - min($"price"))/stddev($"price"), 2) as 'min_stddev
//      )
//      .orderBy($"min_stddev".desc)
//      .show(20, false)
//
    spark.sparkContext.hadoopConfiguration.set("fs.s3a.endpoint", "http://localhost:9001")
    spark.sparkContext.hadoopConfiguration.set("fs.s3a.access.key", "minio")
    spark.sparkContext.hadoopConfiguration.set("fs.s3a.secret.key", "minio123")
    spark.sparkContext.hadoopConfiguration.set("fs.s3a.path.style.access", "true")
    spark.sparkContext.hadoopConfiguration.set("fs.s3a.connection.ssl.enabled", "false")
    spark.sparkContext.hadoopConfiguration.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
//
//    val hudi_spot_priceDF = spark.
//      read.
//      format("org.apache.hudi").
//      load("s3a://spotter/hudi" + "/*/*")
//    hudi_spot_priceDF.createOrReplaceTempView("hudi_spot_price")
//    spark.sql("select count(*) from hudi_spot_price where instance = 'a1.xlarge'").show(20, false)
//
//    val hudiA1DF = spark.read.format("org.apache.hudi").load("s3a://spotter/hudi/aws/a1.xlarge")
//    hudiA1DF.createOrReplaceTempView("hudi_a1")
//    spark.sql("select * from hudi_a1 order by timestamp").show(20, false)
//
//    val parquetA1DF = spark.read.format("parquet").load("s3a://spotter/parquet")
//    parquetA1DF.createOrReplaceTempView("parquet_a1")
//    spark.sql("select * from parquet_a1 where instance = 'a1.xlarge' order by timestamp").show(20, false)
//
    import org.apache.spark.sql.SaveMode._
//
//    data.write.partitionBy("provider").mode(Overwrite).option("compression", "gzip").parquet("s3a://spotter/parquet")
//    data.write.partitionBy("provider").mode(Overwrite).csv("s3a://spotter/csv")
//
    import org.apache.hudi.DataSourceWriteOptions._
    import org.apache.hudi.config.HoodieWriteConfig._

    import org.apache.spark.sql.functions.{concat, concat_ws, unix_timestamp}

    val hudiWriteData = data.withColumn("rk", concat($"provider", $"zone", $"instance", unix_timestamp($"timestamp")))
      .withColumn("pp", concat_ws("/", $"provider", $"instance"))

    hudiWriteData.write.format("org.apache.hudi")
      .option(HIVE_SYNC_ENABLED_OPT_KEY, "true")
      .option(HIVE_DATABASE_OPT_KEY, "spotter")
      .option(HIVE_TABLE_OPT_KEY, "aws")
      .option(HIVE_URL_OPT_KEY, "jdbc:hive2://localhost:9083")
      .option(PRECOMBINE_FIELD_OPT_KEY, "timestamp")
      .option(RECORDKEY_FIELD_OPT_KEY, "rk")
      .option(PARTITIONPATH_FIELD_OPT_KEY, "pp")
      .option(TABLE_NAME, "spotter")
      .mode(Append)
      .save("s3a://spotter/hudi")

    println(s"took ${Instant.now().minusMillis(startTime.toEpochMilli).toEpochMilli} ms")
  }
}
