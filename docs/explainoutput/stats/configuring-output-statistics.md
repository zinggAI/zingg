# Configuring Output Statistics

[Zingg Enterprise Feature](#user-content-fn-1)[^1]

You can configure stats output either via JSON configuration or via the Python API. Please ensure the path/name contains the placeholder “_$ZINGG\_DYNAMIC\_STAT\_NAME_”. This placeholder will get substituted with the statistics type (SUMMARY, CLUSTER, RECORD) and written to respective locations.&#x20;

### JSON configuration example

```json
{
  "outputStats" : {
       "name":"stats", 
       "format":"csv", 
       "props": {
          "location": "/tmp/zinggStats_$ZINGG_DYNAMIC_STAT_NAME",
          "delimiter": ",",
          "header":true
       }
    }
}
```

### Python configuration example

```python
from zingg import ECsvPipe

statsOutputPipe = ECsvPipe("stats", "/tmp/febrlStats_$ZINGG_DYNAMIC_STAT_NAME")
statsOutputPipe.setHeader("true")
eArgs.setOutputStats(statsOutputPipe)
```

When you execute **match** or **incrementalRun** phases with the output stats pipe, Zingg writes stats for this run under the configured location with timestamp. If the outputStats are not configured, Zingg will not write them and the run will proceed as is.&#x20;

[^1]: Zingg Enterprise is the suite of proprietary products licensed by Zingg. Please refer to https://www.zingg.ai/product/zingg-entity-resolution-compare-versions for individual tier features.
