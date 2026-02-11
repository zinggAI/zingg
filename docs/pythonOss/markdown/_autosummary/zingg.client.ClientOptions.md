# zingg.client.ClientOptions

### *class* zingg.client.ClientOptions(argsSent=None)

Bases: `object`

Class that contains Client options for Zingg object
:param phase: trainMatch, train, match, link, findAndLabel, findTrainingData, recommend etc
:type phase: String
:param args: Parse a list of Zingg command line options parameter values e.g. “–location” etc. optional argument for initializing this class.
:type args: List(String) or None

### Methods

| [`__init__`](#zingg.client.ClientOptions.__init__)                 |                                                                         |
|--------------------------------------------------------------------|-------------------------------------------------------------------------|
| [`getClientOptions`](#zingg.client.ClientOptions.getClientOptions) | Method to get pointer address of this class                             |
| [`getConf`](#zingg.client.ClientOptions.getConf)                   | Method to get CONF value                                                |
| [`getLocation`](#zingg.client.ClientOptions.getLocation)           | Method to get LOCATION value                                            |
| [`getOptionValue`](#zingg.client.ClientOptions.getOptionValue)     | Method to get value for the key option                                  |
| [`getPhase`](#zingg.client.ClientOptions.getPhase)                 | Method to get PHASE value                                               |
| [`hasLocation`](#zingg.client.ClientOptions.hasLocation)           | Method to check if this class has LOCATION parameter set as None or not |
| [`setOptionValue`](#zingg.client.ClientOptions.setOptionValue)     | Method to map option key to the given value                             |
| [`setPhase`](#zingg.client.ClientOptions.setPhase)                 | Method to set PHASE value                                               |

### Attributes

| [`COLUMN`](#zingg.client.ClientOptions.COLUMN)       | Column whose stop words are to be recommended through Zingg   |
|------------------------------------------------------|---------------------------------------------------------------|
| [`CONF`](#zingg.client.ClientOptions.CONF)           | conf parameter for this class                                 |
| [`EMAIL`](#zingg.client.ClientOptions.EMAIL)         | e-mail parameter for this class                               |
| [`LICENSE`](#zingg.client.ClientOptions.LICENSE)     | license parameter for this class                              |
| [`LOCATION`](#zingg.client.ClientOptions.LOCATION)   | location parameter for this class                             |
| [`MODEL_ID`](#zingg.client.ClientOptions.MODEL_ID)   | ZINGG_DIR/MODEL_ID is used to save the model                  |
| [`PHASE`](#zingg.client.ClientOptions.PHASE)         | phase parameter for this class                                |
| [`REMOTE`](#zingg.client.ClientOptions.REMOTE)       | remote option used internally for running on Databricks       |
| [`ZINGG_DIR`](#zingg.client.ClientOptions.ZINGG_DIR) | location where Zingg saves the model, training data etc       |

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

#### \_\_init_\_(argsSent=None)

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
