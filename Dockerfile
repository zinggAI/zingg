FROM docker.io/bitnami/spark:3
ENV SPARK_MASTER local[*]
ENV ZINGG_HOME /zingg-0.3.0-SNAPSHOT
WORKDIR /
USER root
RUN curl --location https://github.com/zinggAI/zingg/releases/download/v0.3.0/zingg-0.3.0-SNAPSHOT-spark-3.0.3.tar.gz | \
tar --extract --gzip --strip=1 
#RUN chmod +x zingg-0.3.0-SNAPSHOT-spark-3.0.3.tar.gz
#RUN tar --extract --gzip --strip=1 /tmp/zingg-0.3.0-SNAPSHOT-spark-3.0.3.tar.gz 
USER 1001
WORKDIR /zingg-0.3.0-SNAPSHOT
