docker build -f base.Dockerfile -t quay.io/ahappypie/presto/330:base .
docker build -f coordinator.Dockerfile  -t quay.io/ahappypie/presto/330:coordinator .
docker build -f worker.Dockerfile -t quay.io/ahappypie/presto/330:worker .
