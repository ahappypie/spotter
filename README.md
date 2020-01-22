# Spotter
Experiments with Spark, Kafka, Avro, Schema Registry, Hudi and others

### Setup Hudi (Optional once Hudi releases Scala 2.12 artifacts)
* Clone `apache/incubator-hudi`
* Build Hudi: ```dev/change-scala-version.sh 2.12 && mvn -Pscala-2.12 -DskipITs clean install```
* Point Hudi dependency at local file

### Development Environment
* See `ahappypie/aws-spot` for data generation and Kafka/Zookeeper/Schema Registry compose file (may need to `docker create network spotter`)
* Bring up minio: `docker-compose up -d`