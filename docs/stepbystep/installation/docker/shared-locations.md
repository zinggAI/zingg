---
description: Shared location used to store Zingg configurations
---

# Shared Locations

## Zingg Configurations Using Shared Location

The **zinggDir** location where model information is stored may use a shared location. In fact, any _oft-editable_ file such as **config.json** should be kept in this location only.

```
zingg.sh --phase label --conf config.json --zinggDir /location
```

Similarly, the output and data dir [configurations](../../configuration/) inside **config.json** can be made using a shared location. Please ensure that the running user has access permissions for this location.
