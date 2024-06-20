# Run Incremental load for newer and incremental datasets

Zingg enterprise offers a new feature of running incremental load on already matched dataset. As new data arrives there is no need to run the match phase on entire data. Instead we can reuse the models created earlier on newer and incremental datasets. This will assign clusters on new data sets as well as update existing dataset and reassign clusters if needed.

You can invoke the incremental phase by invoking\
`./scripts/zingg.sh --phase runIncremental --conf <location to incrementalConf.json>`

Example incrementalConf.json:

{	
	"config" : "<parent config used for match>",
	"incrementalData": <data pipe for incremental data>
}

runIncremental can also be triggerred using python by invoking\
`./scripts/zingg.sh --run examples/FebrlExample.py`

Python code example:

incrArgs = IncrementalArguments()
incrArgs.setParentArgs(args)
incrPipe = ECsvPipe("testFebrlIncr", "examples/febrl/test-incr.csv", schema)
incrArgs.setIncrementalData(incrPipe)

