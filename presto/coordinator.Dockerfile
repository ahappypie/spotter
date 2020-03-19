ARG PRESTO_VERSION=330

FROM quay.io/ahappypie/presto/${PRESTO_VERSION}:base

ADD coordinator/ $PRESTO_HOME/server/etc

ADD startup.sh $PRESTO_HOME/server

RUN chmod +x $PRESTO_HOME/server/startup.sh

EXPOSE 8080

CMD $PRESTO_HOME/server/startup.sh
