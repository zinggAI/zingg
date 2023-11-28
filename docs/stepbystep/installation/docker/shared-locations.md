---
description: Shared location used to store Zingg configurations
---

# Shared locations

## Zingg configurations using shared location

The **zinggDir** location where model information is stored may use a shared location. In fact, any oft-editable file such as config.json should be kept in this location only.

```
zingg.sh --phase label --conf config.json --zinggDir /location
```

Similarly, the output and data dir [configurations](../../../stepbystep/configuration) inside config.json can be made using a shared location. Please ensure that the running user has access permissions for this location.
