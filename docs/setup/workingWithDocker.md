
# Working with Zingg docker image
Running Zingg in Docker container is straightforward. Run following commands to get into the container. 
```
docker pull zingg/zingg:0.3.3
docker run -it zingg/zingg:0.3.3 bash
```
## Sharing custom data and config files 
However, note that once docker container is stopped, all the work done in that session is lost. If we want to use custom data or persist the generated model or data files, we have to use **Volumes** or **Bind mount** to share files between the two.

```
docker run -v <local-location>:<container-location> -it zingg/zingg:0.3.3 bash
```
The **\<local-location\>** directory from host will get mounted inside container at **\<container-location\>**. Any file written inside this directory will persist on host machine and can be reused in new container instance later.

## Zingg configurations using shared location

The **zinggDir** location where model inforamtion is stored may use shared location. In fact, any oft-editable file such as config.json should be kept in this location only.
```
zingg.sh --phase label --conf config.json --zinggDir /location
```
Similarly, the output and data dir [configurations](setup/configuration.md) inside config.json can be made using shared location. Please ensure that the running user has access permissions for this location.

## File read/write permissions 
A docker image is preferred to run with non-root user. By default, the zingg container runs with uid '1001'. A valid 'uid' can be passed through command line in order to run the container with that userid. This will enable the user to have requisite permissions to create/read/write files in shared location.
```
$ id 
uid=1000(abc) gid=1000(abc) groups=1000(abc)
$ docker run -u <uid> -it zingg/zingg:0.3.3 bash
```

## Copying files To and From the Container

A quick alternative to **Volume/bind Mount** is to just copy necessary files to and forth between the host and the container.

One specific file/directory can be copied TO and FROM the container. e.g.
```
$ docker cp foo.txt <container_id>:/foo.txt
$ docker cp <container_id>:/foo.txt foo.txt
```
The container id of the running instance can be found using below command.
```
$ docker ps
```
To know more about Docker, please refer to official [docker documentation](https://docs.docker.com/)

## Running Zingg within the docker image
Please follow the [Step By Step guide](stepByStep.md) to run Zingg commands inside the container.
