---
description: Using custom data to save data files on host machine
---

# Sharing Custom Data And Config Files

However, note that once the docker container is stopped, all the work done in that session is lost. If we want to use custom data or persist the generated model or data files, we have to use **Volumes** or **Bind Mount** to share files between the two.

```
docker run -v <local-location>:<container-location> -it zingg/zingg:0.4.1-SNAPSHOT bash
```

The **\<local-location>** directory from host will get mounted inside container at **\<container-location>**. Any file written inside this directory will persist on the host machine and can be reused in a new container instance later.
