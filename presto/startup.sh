#!/bin/bash

sed 's/${env:NODE_ID}'"/$NODE_ID/g" -i $PRESTO_HOME/server/etc/node.properties

$PRESTO_HOME/server/bin/launcher run
