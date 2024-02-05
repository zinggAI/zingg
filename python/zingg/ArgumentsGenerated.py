from zingg.otherThanGenerated import *
'''
This class helps supply match arguments to Zingg. There are 3 basic steps
 in any match process.
 <ul>
 <li>Defining - specifying information about data location, fields and our
 notion of similarity.
 <li>Training - making Zingg learn the matching rules
 <li>Matching - Running the models on entire dataset
 </ul>
 <p>
 There is another step, creating labeled data, which can be used to create
 training data if none is present. Let us cover them in greater detail through
 an example.
 <p>
 We have some positive and negative labeled examples from which we want
 Zingg to learn. These are saved in
 <p>
 /path/to/training/data/positive.csv and
 <p>
 /path/to/training/data/negative.csv
 <p>
 Our actual data has colA,colB,colC,colD,colE with comma as the delimiter and
 is saved at
 <p>
 /path/to/match/data.csv.
 <p>
 We want to match on colB and colD only, one of which is String and other is
 int
 <p>
 Our program would look like
 
 <pre>
 {
 	&#064;code
 	Arguments args = new Arguments();
 	args.setDelimiter(&quot;,&quot;);
 	args.setPositiveTrainingSamples(&quot;/path/to/training/data/positive.csv&quot;);
 	args.setNegativeTrainingSamples(&quot;/path/to/training/data/negative.csv&quot;);
 
 	FieldDefinition colB = new FieldDefinition(1, FieldClass.STRING,
 			FieldType.WORD);
 	FieldDefinition colD = new FieldDefinition(3, FieldClass.INTEGER,
 			FieldType.NUMERIC);
 
 	List&lt;FieldDefinition&gt; fields = new ArrayList&lt;FieldDefinition&gt;();
 	fields.add(colB);
 	fields.add(colD);
 	args.setFieldDefinition(fields);
 
 	args.setMatchData(&quot;/path/to/match/data.csv&quot;);
 
 	args.setZinggDir(&quot;/path/to/models&quot;);
 	args.setOutputDir(&quot;/path/to/match/output&quot;);
 
 	Client client = new Client(args, &quot;local&quot;);
 	client.train();
 	client.run();
 }
 </pre>
'''
class Arguments:
    def __init__(self):
        self.arguments = getJVM().zingg.common.client.Arguments()

    def setNumPartitions(self, numPartitions):
        self.arguments.setNumPartitions(numPartitions)

    '''
Set the fraction of data to be used from complete data set to be used for
 seeding the labelled data Labelling is costly and we want a fast
 approximate way of looking at a small sample of the records and
 identifying expected matches and non matches
 
 @param labelDataSampleSize
            - float between 0 and 1 denoting portion of dataset to use in
            generating seed samples
 @throws ZinggClientException
    '''
    def setLabelDataSampleSize(self, labelDataSampleSize):
        self.arguments.setLabelDataSampleSize(labelDataSampleSize)

    '''
Location for internal Zingg use.
 
 @return the path for internal Zingg usage
	 
	public Pipe[] getZinggInternal() {
		return zinggInternal;
	}

	/**
 Set the location for Zingg to save its internal computations and
 models. Please set it to a place where the program has write access.
 
 @param zinggDir
            path to the Zingg directory
	 
	public void setZinggInternal(Pipe[] zinggDir) {
		this.zinggInternal = zinggDir;
	}
    '''
    def getModelId(self):
        return self.arguments.getModelId()

    def setModelId(self, modelId):
        self.arguments.setModelId(modelId)

    '''
Set the output directory where the match result will be saved
 
 @param outputDir
            where the match result is saved
 @throws ZinggClientException
    '''
    def setOutput(self, outputDir):
        self.arguments.setOutput(outputDir)

    '''
Set the location for Zingg to save its internal computations and
 models. Please set it to a place where the program has write access.
 
 @param zinggDir
            path to the Zingg directory
    '''
    def setZinggDir(self, zinggDir):
        self.arguments.setZinggDir(zinggDir)

    '''
Location for internal Zingg use.
 
 @return the path for internal Zingg usage
    '''
    def getZinggBaseModelDir(self):
        return self.arguments.getZinggBaseModelDir()

    def getZinggModelDir(self):
        return self.arguments.getZinggModelDir()

    '''
Location for internal Zingg use.
 
 @return the path for internal Zingg usage
    '''
    def getZinggBaseTrainingDataDir(self):
        return self.arguments.getZinggBaseTrainingDataDir()

    '''
Location for internal Zingg use.
 
 @return the path for internal Zingg usage
    '''
    def getZinggTrainingDataUnmarkedDir(self):
        return self.arguments.getZinggTrainingDataUnmarkedDir()

    '''
Location for internal Zingg use.
 
 @return the path for internal Zingg usage
    '''
    def getZinggTrainingDataMarkedDir(self):
        return self.arguments.getZinggTrainingDataMarkedDir()

    def setStopWordsCutoff(self, stopWordsCutoff):
        self.arguments.setStopWordsCutoff(stopWordsCutoff)

    def setColumn(self, column):
        self.arguments.setColumn(column)

