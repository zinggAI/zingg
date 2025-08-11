---
description: From pre-built Docker image with all dependencies included
---

# Docker

## Running From Docker Image From Docker Hub

The easiest way to get started is to pull the Docker image with the last release of Zingg.

```
docker pull zingg/zingg:0.5.1
docker run -it zingg/zingg:0.5.1 bash
```
In case of permission denied, try mapping /tmp of docker with user's machine /tmp
```
docker run -v /tmp:/tmp -it zingg/zingg:0.5.1 bash
```

To know more about Docker, please refer to the official [Docker documentation](https://docs.docker.com/).

##
