# Zingg Entity Resolution Package

Zingg Python APIs for entity resolution, record linkage, data mastering and deduplication using ML
([https://www.zingg.ai](https://www.zingg.ai))

requires python 3.6+; spark 3.5.0
Otherwise, [`zingg.client.Zingg()`](#zingg.client.Zingg) cannot be executed

<a id="module-zingg"></a>

<a id="module-zingg.client"></a>

## zingg.client

This module is the main entry point of the Zingg Python API

### *class* zingg.client.Arguments

Bases: `object`

This class helps supply match arguments to Zingg. There are 3 basic steps in any match process.

* **Defining:**
  specifying information about data location, fields, and our notion of similarity.
* **Training:**
  making Zingg learn the matching rules
* **Matching:**
  Running the models on the entire dataset

#### copyArgs(phase)

#### *static* createArgumentsFromJSON(fileName, phase)

Method to create an object of this class from the JSON file and phase parameter value.

* **Parameters:**
  * **fileName** (*String*) – The CONF parameter value of ClientOption object
  * **phase** (*String*) – The PHASE parameter value of ClientOption object
* **Returns:**
  The pointer containing address of the this class object
* **Return type:**
  pointer([Arguments](#zingg.client.Arguments))

#### *static* createArgumentsFromJSONString(jsonArgs, phase)

#### getArgs()

Method to get pointer address of this class

* **Returns:**
  The pointer containing the address of this class object
* **Return type:**
  pointer([Arguments](#zingg.client.Arguments))

#### getModelId()

#### getZinggBaseModelDir()

#### getZinggBaseTrainingDataDir()

Method to get the location of the folder where Zingg
saves the training data found by findTrainingData

#### getZinggModelDir()

#### getZinggTrainingDataMarkedDir()

Method to get the location of the folder where Zingg
saves the marked training data labeled by the user

#### getZinggTrainingDataUnmarkedDir()

Method to get the location of the folder where Zingg
saves the training data found by findTrainingData

#### setArgs(argumentsObj)

Method to set this class object

* **Parameters:**
  **argumentsObj** (*pointer**(*[*Arguments*](#zingg.client.Arguments)*)*) – Argument object to set this object

#### setColumn(column)

Method to set stopWordsCutoff parameter value
By default, Zingg extracts 10% of the high frequency unique words from a dataset. If user wants different selection, they should set up StopWordsCutoff property

* **Parameters:**
  **stopWordsCutoff** (*float*) – The stop words cutoff parameter value of ClientOption object or file address of json file

#### setData(\*pipes)

Method to set the file path of the file to be matched.

* **Parameters:**
  **pipes** ([*Pipe*](#zingg.pipes.Pipe)*[**]*) – input data pipes separated by comma e.g. (pipe1,pipe2,..)

#### setFieldDefinition(fieldDef)

Method convert python objects to java FieldDefinition objects and set the field definitions associated with this client

* **Parameters:**
  **fieldDef** (*List**(*[*FieldDefinition*](#zingg.client.FieldDefinition)*)*) – python FieldDefinition object list

#### setLabelDataSampleSize(labelDataSampleSize)

Method to set labelDataSampleSize parameter value
Set the fraction of data to be used from the complete data set to be used for seeding the labeled data Labelling is costly and we want a fast approximate way of looking at a small sample of the records and identifying expected matches and nonmatches

* **Parameters:**
  **labelDataSampleSize** (*float*) – value between 0.0 and 1.0 denoting portion of dataset to use in generating seed samples

#### setModelId(id)

Method to set the output directory where the match output will be saved

* **Parameters:**
  **id** (*String*) – model id value

#### setNumPartitions(numPartitions)

Method to set NumPartitions parameter value
Sample size to use for seeding labeled data We don’t want to run over all the data, as we want a quick way to seed some labeled data that we can manually edit

* **Parameters:**
  **numPartitions** (*int*) – number of partitions for given data pipes

#### setOutput(\*pipes)

Method to set the output directory where the match result will be saved

* **Parameters:**
  **pipes** ([*Pipe*](#zingg.pipes.Pipe)*[**]*) – output data pipes separated by comma e.g. (pipe1,pipe2,..)

#### setStopWordsCutoff(stopWordsCutoff)

Method to set stopWordsCutoff parameter value
By default, Zingg extracts 10% of the high frequency unique words from a dataset. If user wants different selection, they should set up StopWordsCutoff property

* **Parameters:**
  **stopWordsCutoff** (*float*) – The stop words cutoff parameter value of ClientOption object or file address of json file

#### setTrainingSamples(\*pipes)

Method to set existing training samples to be matched.

* **Parameters:**
  **pipes** ([*Pipe*](#zingg.pipes.Pipe)*[**]*) – input training data pipes separated by comma e.g. (pipe1,pipe2,..)

#### setZinggDir(f)

Method to set the location for Zingg to save its internal computations and models. Please set it to a place where the program has to write access.

* **Parameters:**
  **f** (*String*) – Zingg directory name of the models

#### writeArgumentsToJSON(fileName)

Method to write JSON file from the object of this class

* **Parameters:**
  **fileName** (*String*) – The CONF parameter value of ClientOption object or file address of json file

#### writeArgumentsToJSONString()

Method to create an object of this class from the JSON file and phase parameter value.

* **Parameters:**
  * **fileName** (*String*) – The CONF parameter value of ClientOption object
  * **phase** (*String*) – The PHASE parameter value of ClientOption object
* **Returns:**
  The pointer containing address of the this class object
* **Return type:**
  pointer([Arguments](#zingg.client.Arguments))

### *class* zingg.client.ClientOptions(argsSent=None)

Bases: `object`

Class that contains Client options for Zingg object
:param phase: trainMatch, train, match, link, findAndLabel, findTrainingData, recommend etc
:type phase: String
:param args: Parse a list of Zingg command line options parameter values e.g. “–location” etc. optional argument for initializing this class.
:type args: List(String) or None

#### COLUMN *= <py4j.java_gateway.JavaPackage object>*

Column whose stop words are to be recommended through Zingg

* **Type:**
  COLUMN

#### CONF *= <py4j.java_gateway.JavaPackage object>*

conf parameter for this class

* **Type:**
  CONF

#### EMAIL *= <py4j.java_gateway.JavaPackage object>*

e-mail parameter for this class

* **Type:**
  EMAIL

#### LICENSE *= <py4j.java_gateway.JavaPackage object>*

license parameter for this class

* **Type:**
  LICENSE

#### LOCATION *= <py4j.java_gateway.JavaPackage object>*

location parameter for this class

* **Type:**
  LOCATION

#### MODEL_ID *= <py4j.java_gateway.JavaPackage object>*

ZINGG_DIR/MODEL_ID is used to save the model

* **Type:**
  MODEL_ID

#### PHASE *= <py4j.java_gateway.JavaPackage object>*

phase parameter for this class

* **Type:**
  PHASE

#### REMOTE *= <py4j.java_gateway.JavaPackage object>*

remote option used internally for running on Databricks

* **Type:**
  REMOTE

#### ZINGG_DIR *= <py4j.java_gateway.JavaPackage object>*

location where Zingg saves the model, training data etc

* **Type:**
  ZINGG_DIR

#### getClientOptions()

Method to get pointer address of this class

* **Returns:**
  The pointer containing address of the this class object
* **Return type:**
  pointer([ClientOptions](#zingg.client.ClientOptions))

#### getConf()

Method to get CONF value

* **Returns:**
  The CONF parameter value
* **Return type:**
  String

#### getLocation()

Method to get LOCATION value

* **Returns:**
  The LOCATION parameter value
* **Return type:**
  String

#### getOptionValue(option)

Method to get value for the key option

* **Parameters:**
  **option** (*String*) – key to geting the value
* **Returns:**
  The value which is mapped for given key
* **Return type:**
  String

#### getPhase()

Method to get PHASE value

* **Returns:**
  The PHASE parameter value
* **Return type:**
  String

#### hasLocation()

Method to check if this class has LOCATION parameter set as None or not

* **Returns:**
  The boolean value if LOCATION parameter is present or not
* **Return type:**
  Bool

#### setOptionValue(option, value)

Method to map option key to the given value

* **Parameters:**
  * **option** (*String*) – key that is mapped with value
  * **value** (*String*) – value to be set for given key

#### setPhase(newValue)

Method to set PHASE value

* **Parameters:**
  **newValue** (*String*) – name of the phase
* **Returns:**
  The pointer containing address of the this class object after seting phase
* **Return type:**
  pointer([ClientOptions](#zingg.client.ClientOptions))

### *class* zingg.client.FieldDefinition(name, dataType, \*matchType)

Bases: `object`

This class defines each field that we use in matching We can use this to configure the properties of each field we use for matching in Zingg.

* **Parameters:**
  * **name** (*String*) – name of the field
  * **dataType** (*String*) – type of the data e.g. string, float, etc.
  * **matchType** (*MatchType*) – match type of this field e.g. FUSSY, EXACT, etc.

#### getFieldDefinition()

Method to get  pointer address of this class

* **Returns:**
  The pointer containing the address of this class object
* **Return type:**
  pointer([FieldDefinition](#zingg.client.FieldDefinition))

#### setStopWords(stopWords)

Method to add stopwords to this class object

* **Parameters:**
  **stopWords** (*String*) – The stop Words containing csv file’s location

#### stringify(str)

Method to stringify’ed the dataType before it is set in FieldDefinition object

* **Parameters:**
  **str** (*String*) – dataType of the FieldDefinition
* **Returns:**
  The stringify’ed value of the dataType
* **Return type:**
  String

### *class* zingg.client.Zingg(args, options)

Bases: `object`

This class is the main point of interface with the Zingg matching product. Construct a client to Zingg using provided arguments and spark master. If running locally, set the master to local.

* **Parameters:**
  * **args** ([*Arguments*](#zingg.client.Arguments)) – arguments for training and matching
  * **options** ([*ClientOptions*](#zingg.client.ClientOptions)) – client option for this class object

#### execute()

Method to execute this class object

#### executeLabel()

Method to run label phase

#### executeLabelUpdate()

Method to run label update phase

#### getArguments()

Method to get atguments of this class object

* **Returns:**
  The pointer containing address of the Arguments object of this class object
* **Return type:**
  pointer([Arguments](#zingg.client.Arguments))

#### getMarkedRecords()

Method to get marked record dataset from the inputpipe

* **Returns:**
  spark dataset containing marked records
* **Return type:**
  Dataset<Row>

#### getMarkedRecordsStat(markedRecords, value)

Method to get No. of records that is marked

* **Parameters:**
  * **markedRecords** (*Dataset<Row>*) – spark dataset containing marked records
  * **value** (*long*) – flag value to check if markedRecord is initially matched or not
* **Returns:**
  The no. of marked records
* **Return type:**
  int

#### getMatchedMarkedRecordsStat()

Method to get No. of records that are marked and matched

* **Returns:**
  The bo. of matched marked records
* **Return type:**
  int

#### getOptions()

Method to get client options of this class object

* **Returns:**
  The pointer containing the address of the ClientOptions object of this class object
* **Return type:**
  pointer([ClientOptions](#zingg.client.ClientOptions))

#### getUnmarkedRecords()

Method to get unmarked record dataset from the inputpipe

* **Returns:**
  spark dataset containing unmarked records
* **Return type:**
  Dataset<Row>

#### getUnmatchedMarkedRecordsStat()

Method to get No. of records that are marked and unmatched

* **Returns:**
  The no. of unmatched marked records
* **Return type:**
  int

#### getUnsureMarkedRecordsStat()

Method to get No. of records that are marked and Not Sure if its matched or not

* **Returns:**
  The no. of Not Sure marked records
* **Return type:**
  int

#### init()

Method to initialize zingg client by reading internal configurations and functions

#### initAndExecute()

Method to run both init and execute methods consecutively

#### processRecordsCli(unmarkedRecords, args)

Method to get user input on unmarked records

* **Returns:**
  spark dataset containing updated records
* **Return type:**
  Dataset<Row>

#### processRecordsCliLabelUpdate(lines, args)

#### setArguments(args)

Method to set Arguments

* **Parameters:**
  **args** ([*Arguments*](#zingg.client.Arguments)) – provide arguments for this class object

#### setOptions(options)

Method to set atguments of this class object

* **Parameters:**
  **options** ([*ClientOptions*](#zingg.client.ClientOptions)) – provide client options for this class object
* **Returns:**
  The pointer containing address of the ClientOptions object of this class object
* **Return type:**
  pointer([ClientOptions](#zingg.client.ClientOptions))

#### writeLabelledOutput(updatedRecords, args)

Method to write updated records after user input

#### writeLabelledOutputFromPandas(candidate_pairs_pd, args)

Method to write updated records (as pandas df) after user input

### *class* zingg.client.ZinggWithSpark(args, options)

Bases: [`Zingg`](#zingg.client.Zingg)

This class is the main point of interface with the Zingg matching product. Construct a client to Zingg using provided arguments and spark master. If running locally, set the master to local.

* **Parameters:**
  * **args** ([*Arguments*](#zingg.client.Arguments)) – arguments for training and matching
  * **options** ([*ClientOptions*](#zingg.client.ClientOptions)) – client option for this class object

### zingg.client.getDfFromDs(data)

Method to convert spark dataset to dataframe

* **Parameters:**
  **data** (*DataSet*) – provide spark dataset
* **Returns:**
  converted spark dataframe
* **Return type:**
  DataFrame

### zingg.client.getGateway()

### zingg.client.getJVM()

### zingg.client.getPandasDfFromDs(data)

Method to convert spark dataset to pandas dataframe

* **Parameters:**
  **data** (*DataSet*) – provide spark dataset
* **Returns:**
  converted pandas dataframe
* **Return type:**
  DataFrame

### zingg.client.getSparkContext()

### zingg.client.getSparkSession()

### zingg.client.getSqlContext()

### zingg.client.initClient()

### zingg.client.initDataBricksConectClient()

### zingg.client.initSparkClient()

### zingg.client.parseArguments(argv)

This method is used for checking mandatory arguments and creating an arguments list from Command line arguments

* **Parameters:**
  **argv** (*List*) – Values that are passed during the calling of the program along with the calling statement.
* **Returns:**
  a list containing necessary arguments to run any phase
* **Return type:**
  List

<a id="module-zingg.pipes"></a>

## zingg.pipes

This module is submodule of zingg to work with different types of Pipes. Classes of this module inherit the Pipe class, and use that class to create many different types of pipes.

### *class* zingg.pipes.BigQueryPipe(name)

Bases: [`Pipe`](#zingg.pipes.Pipe)

Pipe Class for working with BigQuery pipeline

* **Parameters:**
  **name** (*String*) – name of the pipe.

#### CREDENTIAL_FILE *= 'credentialsFile'*

#### TABLE *= 'table'*

#### TEMP_GCS_BUCKET *= 'temporaryGcsBucket'*

#### VIEWS_ENABLED *= 'viewsEnabled'*

#### setCredentialFile(file)

Method to set Credential file to the pipe

* **Parameters:**
  **file** (*String*) – credential file name

#### setTable(table)

Method to set Table to the pipe

* **Parameters:**
  **table** (*String*) – provide table parameter

#### setTemporaryGcsBucket(bucket)

Method to set TemporaryGcsBucket to the pipe

* **Parameters:**
  **bucket** (*String*) – provide bucket parameter

#### setViewsEnabled(isEnabled)

Method to set if viewsEnabled parameter is Enabled or not

* **Parameters:**
  **isEnabled** (*Bool*) – provide boolean parameter which defines if viewsEnabled option is enable or not

### *class* zingg.pipes.CsvPipe(name, location=None, schema=None)

Bases: [`Pipe`](#zingg.pipes.Pipe)

Class CsvPipe: used for working with text files which uses a pipe symbol to separate units of text that belong in different columns.

* **Parameters:**
  * **name** (*String*) – name of the pipe.
  * **location** (*String* *or* *None*) – (optional) location from where we read data
  * **schema** (*Schema* *or* *None*) – (optional) json schema for the pipe

#### setDelimiter(delimiter)

This method is used to define delimiter of CsvPipe

* **Parameters:**
  **delimiter** (*String*) – a sequence of one or more characters for specifying the boundary between separate, independent regions in data streams

#### setHeader(header)

Method to set header property of pipe

* **Parameters:**
  **header** (*Boolean*) – true if pipe have header, false otherwise

#### setLocation(location)

Method to set location of pipe

* **Parameters:**
  **location** (*String*) – location from where we read data

### *class* zingg.pipes.Pipe(name, format)

Bases: `object`

Pipe class for working with different data-pipelines. Actual pipe def in the args. One pipe can be used at multiple places with different tables, locations, queries, etc

* **Parameters:**
  * **name** (*String*) – name of the pipe
  * **format** (*Format*) – formate of pipe e.g. bigquery,csv, etc.

#### addProperty(name, value)

Method for adding different properties of pipe

* **Parameters:**
  * **name** (*String*) – name of the property
  * **value** (*String*) – value you want to set for the property

#### getPipe()

Method to get Pipe

* **Returns:**
  pipe parameter values in the format of a list of string
* **Return type:**
  [Pipe](#zingg.pipes.Pipe)

#### setSchema(s)

Method to set pipe schema value

* **Parameters:**
  **s** (*Schema*) – json schema for the pipe

#### toString()

Method to get pipe parameter values

* **Returns:**
  pipe information in list format
* **Return type:**
  List[String]

### *class* zingg.pipes.SnowflakePipe(name)

Bases: [`Pipe`](#zingg.pipes.Pipe)

Pipe Class for working with Snowflake pipeline

* **Parameters:**
  **name** (*String*) – name of the pipe

#### DATABASE *= 'sfDatabase'*

#### DBTABLE *= 'dbtable'*

#### PASSWORD *= 'sfPassword'*

#### SCHEMA *= 'sfSchema'*

#### URL *= 'sfUrl'*

#### USER *= 'sfUser'*

#### WAREHOUSE *= 'sfWarehouse'*

#### setDatabase(db)

Method to set Database to the pipe

* **Parameters:**
  **db** (*Database*) – provide Database parameter.

#### setDbTable(dbtable)

description

* **Parameters:**
  **dbtable** (*String*) – provide bucket parameter.

#### setPassword(passwd)

Method to set Password to the pipe

* **Parameters:**
  **passwd** (*String*) – provide Password parameter.

#### setSFSchema(schema)

Method to set Schema to the pipe

* **Parameters:**
  **schema** (*Schema*) – provide schema parameter.

#### setURL(url)

Method to set url to the pipe

* **Parameters:**
  **url** (*String*) – provide url for this pipe

#### setUser(user)

Method to set User to the pipe

* **Parameters:**
  **user** (*String*) – provide User parameter.

#### setWarehouse(warehouse)

Method to set warehouse parameter to the pipe

* **Parameters:**
  **warehouse** (*String*) – provide warehouse parameter.
