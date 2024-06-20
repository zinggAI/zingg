# Run Incremental Loads For New and Updated Records - Zingg Enterprise Only Feature

Rerunning matching on entire datasets is wasteful, and we lose the lineage of matched records against a persistent identifier. Using Zingg Enterprise, incremental loads can be run to match existing pre resolved entities. The new and updated records are matched to existing clusters, and new persistent ZINGG_IDs generated for records which do not find a match. If a record gets updated and Zingg Enterprise discovers that it is a more suitable match with another cluster, it will be reassigned. Cluster assignment, merge and unmerge happens automatically in the flow. Zingg Entperirse also takes care of human feedback on previously matched data to ensure that it doesnt override the approved records. 

The incremental phase is run as follows\
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

