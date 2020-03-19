ARG HIVE_VERSION

FROM openjdk:8-jre-slim

ENV HIVE_VERSION=${HIVE_VERSION:-2.3.6}

ARG HADOOP_VERSION=2.10.0
ARG HADOOP_URL=https://archive.apache.org/dist/hadoop/common/hadoop-${HADOOP_VERSION}/hadoop-${HADOOP_VERSION}.tar.gz
ENV HADOOP_VERSION ${HADOOP_VERSION}
ENV HADOOP_URL ${HADOOP_URL}
ENV HADOOP_HOME /opt/hadoop-${HADOOP_VERSION}

ENV HIVE_HOME /opt/hive
ENV PATH $HIVE_HOME/bin:$PATH

WORKDIR /opt

#Install Hive, MySQL JDBC, Hudi Hive Bundle
RUN apt-get update && apt-get install -y wget && \
    wget $HADOOP_URL -O hadoop-${HADOOP_VERSION}.tar.gz && \
    tar -xzvf hadoop-${HADOOP_VERSION}.tar.gz && \
    rm hadoop-${HADOOP_VERSION}.tar.gz && \
	wget https://archive.apache.org/dist/hive/hive-$HIVE_VERSION/apache-hive-$HIVE_VERSION-bin.tar.gz && \
	tar -xzvf apache-hive-$HIVE_VERSION-bin.tar.gz && \
	mv apache-hive-$HIVE_VERSION-bin hive && \
	wget https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.47/mysql-connector-java-5.1.47.jar -O $HIVE_HOME/lib/mysql-connector-java-5.1.47.jar && \
	wget https://repo1.maven.org/maven2/org/apache/hudi/hudi-hive-bundle/0.5.1-incubating/hudi-hive-bundle-0.5.1-incubating.jar -O $HIVE_HOME/lib/hive-hudi-bundle-0.5.1-incubating.jar && \
	rm apache-hive-$HIVE_VERSION-bin.tar.gz && \
	apt-get --purge remove -y wget && \
	apt-get clean && \
	rm -rf /var/lib/apt/lists/*

ADD hive-site.xml $HIVE_HOME/conf

EXPOSE 9083

CMD hive --service metastore --hiveconf hive.aux.jars.path=file://${HIVE_HOME}/lib/hudi-hive-bundle-0.5.1-incubating.jar