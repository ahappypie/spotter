FROM openjdk:11-jdk-slim

ARG VERSION
ENV PRESTO_VERSION=${VERSION:-330}
ENV PRESTO_HOME=/presto

RUN apt update && apt install wget python -y

WORKDIR /presto

RUN wget https://repo1.maven.org/maven2/io/prestosql/presto-server/${PRESTO_VERSION}/presto-server-${PRESTO_VERSION}.tar.gz -O presto-server-${PRESTO_VERSION}.tar.gz
RUN mkdir server
RUN tar -xvzf presto-server-${PRESTO_VERSION}.tar.gz -C server/ --strip-components=1
RUN rm presto-server-${PRESTO_VERSION}.tar.gz
