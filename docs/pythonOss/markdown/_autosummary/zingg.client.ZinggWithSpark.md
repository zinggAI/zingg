# zingg.client.ZinggWithSpark

### *class* zingg.client.ZinggWithSpark(args, options)

Bases: [`Zingg`](zingg.client.Zingg.md#zingg.client.Zingg)

This class is the main point of interface with the Zingg matching product. Construct a client to Zingg using provided arguments and spark master. If running locally, set the master to local.

* **Parameters:**
  * **args** ([*Arguments*](zingg.client.Arguments.md#zingg.client.Arguments)) – arguments for training and matching
  * **options** ([*ClientOptions*](zingg.client.ClientOptions.md#zingg.client.ClientOptions)) – client option for this class object

### Methods

| [`__init__`](#zingg.client.ZinggWithSpark.__init__)                                           |                                                                                    |
|-----------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| [`execute`](#zingg.client.ZinggWithSpark.execute)                                             | Method to execute this class object                                                |
| [`executeLabel`](#zingg.client.ZinggWithSpark.executeLabel)                                   | Method to run label phase                                                          |
| [`executeLabelUpdate`](#zingg.client.ZinggWithSpark.executeLabelUpdate)                       | Method to run label update phase                                                   |
| [`getArguments`](#zingg.client.ZinggWithSpark.getArguments)                                   | Method to get atguments of this class object                                       |
| [`getMarkedRecords`](#zingg.client.ZinggWithSpark.getMarkedRecords)                           | Method to get marked record dataset from the inputpipe                             |
| [`getMarkedRecordsStat`](#zingg.client.ZinggWithSpark.getMarkedRecordsStat)                   | Method to get No.                                                                  |
| [`getMatchedMarkedRecordsStat`](#zingg.client.ZinggWithSpark.getMatchedMarkedRecordsStat)     | Method to get No.                                                                  |
| [`getOptions`](#zingg.client.ZinggWithSpark.getOptions)                                       | Method to get client options of this class object                                  |
| [`getUnmarkedRecords`](#zingg.client.ZinggWithSpark.getUnmarkedRecords)                       | Method to get unmarked record dataset from the inputpipe                           |
| [`getUnmatchedMarkedRecordsStat`](#zingg.client.ZinggWithSpark.getUnmatchedMarkedRecordsStat) | Method to get No.                                                                  |
| [`getUnsureMarkedRecordsStat`](#zingg.client.ZinggWithSpark.getUnsureMarkedRecordsStat)       | Method to get No.                                                                  |
| [`init`](#zingg.client.ZinggWithSpark.init)                                                   | Method to initialize zingg client by reading internal configurations and functions |
| [`initAndExecute`](#zingg.client.ZinggWithSpark.initAndExecute)                               | Method to run both init and execute methods consecutively                          |
| [`processRecordsCli`](#zingg.client.ZinggWithSpark.processRecordsCli)                         | Method to get user input on unmarked records                                       |
| `processRecordsCliLabelUpdate`                                                                |                                                                                    |
| [`setArguments`](#zingg.client.ZinggWithSpark.setArguments)                                   | Method to set Arguments                                                            |
| [`setOptions`](#zingg.client.ZinggWithSpark.setOptions)                                       | Method to set atguments of this class object                                       |
| [`writeLabelledOutput`](#zingg.client.ZinggWithSpark.writeLabelledOutput)                     | Method to write updated records after user input                                   |
| [`writeLabelledOutputFromPandas`](#zingg.client.ZinggWithSpark.writeLabelledOutputFromPandas) | Method to write updated records (as pandas df) after user input                    |

#### \_\_init_\_(args, options)

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
  pointer([Arguments](zingg.client.Arguments.md#zingg.client.Arguments))

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
  pointer([ClientOptions](zingg.client.ClientOptions.md#zingg.client.ClientOptions))

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

#### setArguments(args)

Method to set Arguments

* **Parameters:**
  **args** ([*Arguments*](zingg.client.Arguments.md#zingg.client.Arguments)) – provide arguments for this class object

#### setOptions(options)

Method to set atguments of this class object

* **Parameters:**
  **options** ([*ClientOptions*](zingg.client.ClientOptions.md#zingg.client.ClientOptions)) – provide client options for this class object
* **Returns:**
  The pointer containing address of the ClientOptions object of this class object
* **Return type:**
  pointer([ClientOptions](zingg.client.ClientOptions.md#zingg.client.ClientOptions))

#### writeLabelledOutput(updatedRecords, args)

Method to write updated records after user input

#### writeLabelledOutputFromPandas(candidate_pairs_pd, args)

Method to write updated records (as pandas df) after user input
