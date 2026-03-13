# zingg.client.Arguments

### *class* zingg.client.Arguments

Bases: `object`

This class helps supply match arguments to Zingg. There are 3 basic steps in any match process.

* **Defining:**
  specifying information about data location, fields, and our notion of similarity.
* **Training:**
  making Zingg learn the matching rules
* **Matching:**
  Running the models on the entire dataset

### Methods

| [`__init__`](#zingg.client.Arguments.__init__)                                               |                                                                                                                                                                                                                                                                                                             |
|----------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `copyArgs`                                                                                   |                                                                                                                                                                                                                                                                                                             |
| [`createArgumentsFromJSON`](#zingg.client.Arguments.createArgumentsFromJSON)                 | Method to create an object of this class from the JSON file and phase parameter value.                                                                                                                                                                                                                      |
| `createArgumentsFromJSONString`                                                              |                                                                                                                                                                                                                                                                                                             |
| [`getArgs`](#zingg.client.Arguments.getArgs)                                                 | Method to get pointer address of this class                                                                                                                                                                                                                                                                 |
| `getModelId`                                                                                 |                                                                                                                                                                                                                                                                                                             |
| `getZinggBaseModelDir`                                                                       |                                                                                                                                                                                                                                                                                                             |
| [`getZinggBaseTrainingDataDir`](#zingg.client.Arguments.getZinggBaseTrainingDataDir)         | Method to get the location of the folder where Zingg saves the training data found by findTrainingData                                                                                                                                                                                                      |
| `getZinggModelDir`                                                                           |                                                                                                                                                                                                                                                                                                             |
| [`getZinggTrainingDataMarkedDir`](#zingg.client.Arguments.getZinggTrainingDataMarkedDir)     | Method to get the location of the folder where Zingg saves the marked training data labeled by the user                                                                                                                                                                                                     |
| [`getZinggTrainingDataUnmarkedDir`](#zingg.client.Arguments.getZinggTrainingDataUnmarkedDir) | Method to get the location of the folder where Zingg saves the training data found by findTrainingData                                                                                                                                                                                                      |
| [`setArgs`](#zingg.client.Arguments.setArgs)                                                 | Method to set this class object                                                                                                                                                                                                                                                                             |
| [`setColumn`](#zingg.client.Arguments.setColumn)                                             | Method to set stopWordsCutoff parameter value By default, Zingg extracts 10% of the high frequency unique words from a dataset.                                                                                                                                                                             |
| [`setData`](#zingg.client.Arguments.setData)                                                 | Method to set the file path of the file to be matched.                                                                                                                                                                                                                                                      |
| [`setFieldDefinition`](#zingg.client.Arguments.setFieldDefinition)                           | Method convert python objects to java FieldDefinition objects and set the field definitions associated with this client                                                                                                                                                                                     |
| [`setLabelDataSampleSize`](#zingg.client.Arguments.setLabelDataSampleSize)                   | Method to set labelDataSampleSize parameter value Set the fraction of data to be used from the complete data set to be used for seeding the labeled data Labelling is costly and we want a fast approximate way of looking at a small sample of the records and identifying expected matches and nonmatches |
| [`setModelId`](#zingg.client.Arguments.setModelId)                                           | Method to set the output directory where the match output will be saved                                                                                                                                                                                                                                     |
| [`setNumPartitions`](#zingg.client.Arguments.setNumPartitions)                               | Method to set NumPartitions parameter value Sample size to use for seeding labeled data We don't want to run over all the data, as we want a quick way to seed some labeled data that we can manually edit                                                                                                  |
| [`setOutput`](#zingg.client.Arguments.setOutput)                                             | Method to set the output directory where the match result will be saved                                                                                                                                                                                                                                     |
| [`setStopWordsCutoff`](#zingg.client.Arguments.setStopWordsCutoff)                           | Method to set stopWordsCutoff parameter value By default, Zingg extracts 10% of the high frequency unique words from a dataset.                                                                                                                                                                             |
| [`setTrainingSamples`](#zingg.client.Arguments.setTrainingSamples)                           | Method to set existing training samples to be matched.                                                                                                                                                                                                                                                      |
| [`setZinggDir`](#zingg.client.Arguments.setZinggDir)                                         | Method to set the location for Zingg to save its internal computations and models.                                                                                                                                                                                                                          |
| [`writeArgumentsToJSON`](#zingg.client.Arguments.writeArgumentsToJSON)                       | Method to write JSON file from the object of this class                                                                                                                                                                                                                                                     |

#### \_\_init_\_()

#### *static* createArgumentsFromJSON(fileName, phase)

Method to create an object of this class from the JSON file and phase parameter value.

* **Parameters:**
  * **fileName** (*String*) – The CONF parameter value of ClientOption object
  * **phase** (*String*) – The PHASE parameter value of ClientOption object
* **Returns:**
  The pointer containing address of the this class object
* **Return type:**
  pointer([Arguments](#zingg.client.Arguments))

#### getArgs()

Method to get pointer address of this class

* **Returns:**
  The pointer containing the address of this class object
* **Return type:**
  pointer([Arguments](#zingg.client.Arguments))

#### getZinggBaseTrainingDataDir()

Method to get the location of the folder where Zingg
saves the training data found by findTrainingData

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
  **pipes** ([*Pipe*](zingg.pipes.Pipe.md#zingg.pipes.Pipe)*[**]*) – input data pipes separated by comma e.g. (pipe1,pipe2,..)

#### setFieldDefinition(fieldDef)

Method convert python objects to java FieldDefinition objects and set the field definitions associated with this client

* **Parameters:**
  **fieldDef** (*List**(*[*FieldDefinition*](zingg.client.FieldDefinition.md#zingg.client.FieldDefinition)*)*) – python FieldDefinition object list

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
  **pipes** ([*Pipe*](zingg.pipes.Pipe.md#zingg.pipes.Pipe)*[**]*) – output data pipes separated by comma e.g. (pipe1,pipe2,..)

#### setStopWordsCutoff(stopWordsCutoff)

Method to set stopWordsCutoff parameter value
By default, Zingg extracts 10% of the high frequency unique words from a dataset. If user wants different selection, they should set up StopWordsCutoff property

* **Parameters:**
  **stopWordsCutoff** (*float*) – The stop words cutoff parameter value of ClientOption object or file address of json file

#### setTrainingSamples(\*pipes)

Method to set existing training samples to be matched.

* **Parameters:**
  **pipes** ([*Pipe*](zingg.pipes.Pipe.md#zingg.pipes.Pipe)*[**]*) – input training data pipes separated by comma e.g. (pipe1,pipe2,..)

#### setZinggDir(f)

Method to set the location for Zingg to save its internal computations and models. Please set it to a place where the program has to write access.

* **Parameters:**
  **f** (*String*) – Zingg directory name of the models

#### writeArgumentsToJSON(fileName)

Method to write JSON file from the object of this class

* **Parameters:**
  **fileName** (*String*) – The CONF parameter value of ClientOption object or file address of json file
