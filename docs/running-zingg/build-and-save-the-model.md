---
description: >-
  Run the training phase to build Zingg's matching model from your labeled
  training data and save it for reuse.
---

# Build and Save the Model

The training phase builds up the Zingg models using the training data from your label sessions and writes them to `zinggDir/modelId` as specified in your config.

Once saved, reuse the same `modelId` in all subsequent phases - `match`, `link`, and `runIncremental` to apply this trained model to your data.

{% hint style="success" icon="right-long" %}
Model saved to: `zinggDir/modelId`

Use the same `modelId` when running `match`, `link`, or `runIncremental` to apply this trained model.
{% endhint %}

{% tabs %}
{% tab title="Community" %}
### Python

```python
options = ClientOptions([ClientOptions.PHASE, "train"])
zingg = Zingg(args, options)
zingg.initAndExecute()
```

### CLI

```bash
./scripts/zingg.sh --phase train --conf config.json
```
{% endtab %}

{% tab title="Enterprise" %}
### Python

```python
options = ClientOptions([ClientOptions.PHASE, "train"])
zingg = EZingg(args, options)
zingg.initAndExecute()
```

### CLI

```bash
./scripts/zingg.sh --phase train --conf config.json
```
{% endtab %}

{% tab title="Enterprise Snowflake" %}

### Run train

Zingg on Snowflake can be run either from a local terminal via the CLI, or natively inside Snowflake as a job service.

#### CLI (local terminal)

```bash
./scripts/zingg.sh --phase train --conf config.json \
--properties-file <location to snowflake.properties>
```

#### Snowflake (job service)

Run the phase as an asynchronous job service inside your Snowflake compute pool.

```sql
EXECUTE JOB SERVICE
IN COMPUTE POOL CONTAINER_ZINGG_POOL
NAME = ZINGG_TRAIN_ASYNC_JOB_SERVICE
ASYNC = true
EXTERNAL_ACCESS_INTEGRATIONS = (ALLOW_ALL_EAI)
FROM @specs SPECIFICATION_TEMPLATE_FILE = '<zingg-job-spec-file>'
USING (PHASE => 'train', CONFIG => '<config-name>');
```

`<zingg-job-spec-file>` defines the container and job service configuration Zingg needs to run inside Snowpark Container Services. It is maintained by your administrator and referenced from your `@specs` stage.

`<config-name>` is the name of your Zingg configuration json file.

****Monitor the job**

Retrieve the service logs to monitor execution and debug any failures.**

```sql
SELECT SYSTEM$GET_SERVICE_LOGS('ZINGG_TRAIN_ASYNC_JOB_SERVICE', 0, 'zingg-async-job-container');
```

`zingg-async-job-container` refers to the container name defined in the job specification.

**Stop the compute pool**

Once the job completes, stop the compute pool to release resources.

```sql
ALTER COMPUTE POOL CONTAINER_ZINGG_POOL STOP ALL;
```

{% endtab %}
{% endtabs %}