import org.apache.spark.sql.SparkSession

trait SparkSessionWrapper {
  lazy val spark: SparkSession = SparkSession.builder()
    .appName("spotter")
    .master(sys.env.getOrElse("SPARK_MASTER", "local[*]"))
    .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    .getOrCreate()
}
