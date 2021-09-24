FROM docker.io/bitnami/spark:3
WORKDIR /
ADD assembly/target/zingg-0.3.0-SNAPSHOT-spark-3.1.2.tar.gz .
WORKDIR /zingg-0.3.0-SNAPSHOT