---
description: Alternative to volume/bind mount
---

# Copying Files To and From the Container

A quick alternative to **Volume/bind Mount** is to just copy necessary files to and forth between the host and the container.

One specific file/directory can be copied TO and FROM the container. e.g.

```
$ docker cp foo.txt <container_id>:/foo.txt
$ docker cp <container_id>:/foo.txt foo.txt
```

The container id of the running instance can be found using the below command.

```
$ docker ps
```

