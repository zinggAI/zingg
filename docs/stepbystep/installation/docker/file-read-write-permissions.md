# File read/write permissions

A docker image is preferred to run with a non-root user. By default, the Zingg container runs with uid '1001'. A valid 'uid' can be passed through the command line in order to run the container with that user id. This will enable the user to have requisite permissions to create/read/write files in the shared location.

```
$ id 
uid=1000(abc) gid=1000(abc) groups=1000(abc)
$ docker run -u <uid> -it zingg/zingg:0.3.4 bash
```
