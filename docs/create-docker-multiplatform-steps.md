docker multi platform image:

on mac:

    docker build -t <docker-username>/<image-name>:arm64v8 --build-arg ARCH=arm64v8/ .

    docker push <docker-username>/<image-name>:arm64v8


on ubuntu:
      sudo su
      docker login => give username and password

      docker build -t <docker-username>/<image-name>:amd64 --build-arg ARCH=amd64/ .

      docker push <docker-username>/<image-name>:amd64

      docker manifest create \
      <docker-username>/<image-name>:latest \
      --amend <docker-username>/<image-name>:amd64 \
      --amend <docker-username>/<image-name>:arm64v8

      docker manifest push <docker-username>/<image-name>:latest
