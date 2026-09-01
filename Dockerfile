#FROM docker.io/bitnami/spark:3.1.2
FROM apache/spark:3.5.5-python3@sha256:39321d67b23e2e0953f81b60778f74bf40c40a18dfb0e881e6a38593af60afa1
USER 0
RUN apt-get update && \
	apt install -y curl vim 
ENV SPARK_MASTER local[*]
ENV ZINGG_HOME /zingg-0.7.0
ENV PATH $ZINGG_HOME/scripts:$PATH
ENV LANG C.UTF-8
WORKDIR /
USER root
WORKDIR /zingg-0.7.0
RUN curl --fail --show-error --silent --location \
    --output /tmp/zingg-0.7.0-spark-3.5.5.tar.gz \
    https://github.com/zinggAI/zingg/releases/download/v0.7.0/zingg-0.7.0-spark-3.5.5.tar.gz && \
    echo '4765f43e84f81728078037c65a12d119d5703366328ccabbd2357d8fecf0c5c3  /tmp/zingg-0.7.0-spark-3.5.5.tar.gz' | sha256sum --check --strict && \
    tar --extract --gzip --file /tmp/zingg-0.7.0-spark-3.5.5.tar.gz --strip=1 && \
    rm --force /tmp/zingg-0.7.0-spark-3.5.5.tar.gz
RUN pip install -r python/requirements.txt
RUN pip install zingg
RUN chown -R spark:spark /zingg-0.7.0/models && \
    find /zingg-0.7.0/models -type d -exec chmod 0770 {} + && \
    find /zingg-0.7.0/models -type f -exec chmod 0660 {} +
USER spark

