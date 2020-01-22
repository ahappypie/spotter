object Spotter extends SparkSessionWrapper {
  def main(args: Array[String]): Unit = {
    val schemaRegistryURL = "http://localhost:8081"
    val topic = "spot-price-topic"

    val df = spark.read
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", topic)
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

    import org.apache.spark.sql.functions.{min, avg, max, stddev, round}

    data.filter($"instance" like "%dn%")
      .groupBy($"instance")
      .agg(
        min($"price"),
        round(avg($"price"), 4),
        max($"price"),
        round(stddev($"price"), 4),
        round((avg($"price") - min($"price"))/stddev($"price"), 2) as 'min_stddev
      )
      .orderBy($"min_stddev".desc)
      .show(20, false)

    spark.sparkContext.hadoopConfiguration.set("fs.s3a.endpoint", "http://localhost:9001")
    spark.sparkContext.hadoopConfiguration.set("fs.s3a.access.key", "minio")
    spark.sparkContext.hadoopConfiguration.set("fs.s3a.secret.key", "minio123")
    spark.sparkContext.hadoopConfiguration.set("fs.s3a.path.style.access", "true")
    spark.sparkContext.hadoopConfiguration.set("fs.s3a.connection.ssl.enabled", "false")
    spark.sparkContext.hadoopConfiguration.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")

    import org.apache.spark.sql.SaveMode._

    data.write.partitionBy("provider").mode(Overwrite).option("compression", "gzip").parquet("s3a://spotter/parquet")

    import org.apache.hudi.DataSourceWriteOptions._
    import org.apache.hudi.config.HoodieWriteConfig._

    data.write.format("org.apache.hudi").
      options(org.apache.hudi.QuickstartUtils.getQuickstartWriteConfigs).
      option(PRECOMBINE_FIELD_OPT_KEY, "timestamp").
      option(RECORDKEY_FIELD_OPT_KEY, "instance").
      option(PARTITIONPATH_FIELD_OPT_KEY, "zone").
      option(TABLE_NAME, "spotter").
      mode(Overwrite).
      save("s3a://spotter/hudi")
  }
}
