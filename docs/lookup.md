# Lookup Data

[Zingg Enterprise Feature](#user-content-fn-1)[^1]

Sometimes we want to lookup certain records in match output, The lookup feature helps to achieve so. For given lookup records, it assigns zingg-id which decribes which entity cluster it belongs to.

### The lookup phase is run as follows:

`./scripts/zingg.sh --phase runLookup --conf <location to lookupConf.json>`

### Example lookupConf.json:

```json
{
  "config" : "config.json",
  "lookupData": [{
    "name":"lookup-test-data",
    "format":"inMemory"
  }
  ],
  "lookupOutput": [
    {
      "name":"lookup-output",
      "format":"csv",
      "props": {
        "path": "/tmp/zinggOutput/lookup",
        "delimiter": ",",
        "header":true
      }
    }
  ]
} 
```

[^1]: Zingg Enterprise is the suite of proprietary products licensed by Zingg. Please refer to https://www.zingg.ai/product/zingg-entity-resolution-compare-versions for individual tier features.
