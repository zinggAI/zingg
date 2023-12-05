#FROM docker.io/bitnami/spark:3.1.2
FROM apache/spark-py:v3.4.0
USER 0
RUN apt-get update && \
	apt install -y curl vim 
ENV SPARK_MASTER local[*]
ENV ZINGG_HOME /zingg-0.4.0
ENV PATH $ZINGG_HOME/scripts:$PATH
ENV LANG C.UTF-8
WORKDIR /
USER root
WORKDIR /zingg-0.4.0
RUN curl --location https://github.com/zinggAI/zingg/releases/download/v0.4.0/zingg-0.4.0-spark-3.4.0.tar.gz | \
tar --extract --gzip --strip=1 
RUN pip install -r python/requirements.txt
RUN pip install zingg
RUN chmod -R a+rwx /zingg-0.4.0/models
RUN chown -R 1001 /zingg-0.4.0/models
USER 1001

