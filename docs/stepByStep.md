
# Step by step guide 

## Step 1: Install
Installation instructions for docker as well as github release are [here](setup/installation.md). If you need to build from the sources or compile for a different flavor of Spark, check [compiling](setup/installation.md#compiling-from-sources)

## Step 2: Plan for Hardware
Decide your hardware based on the [performance numbers](setup/hardwareSizing.md)

## Step 3: Build the config for your data
Zingg needs a configuration file which defines the data and what kind of matching is needed. You can create the configuration file by following the instructions [here](setup/configuration.md)

## Step 4: Create the training data
Zingg builds a new set of models(blocking and similarity) for every new schema definition(columns and match types). This means running the *findTrainingData* and *label* phases multiple times to build the training dataset form which Zingg will learn. You can read more [here](setup/training/createTrainingData.md)

## Step 5: Build and save the model
The training data in Step 4 above is used to train Zingg and build and save the models. This is done by running the *train* phase. Read more [here](setup/train.md)

## Step 6: Voila, lets match!
Its now time to apply the model above on our data. This si done by running the *match* or the *link* phases depending on whether you are matching within a single source or linking multiple sources respectively. You can read more about [matching](setup/match.md) and [linking](setup/match.md#link)

As long as your input columns and the field types are not changing, the same model should work and you do not need to build a new model. If you change the match type, you can cotinue to use the training data and add more labelled pairs on top of it. 


