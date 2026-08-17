---
description: >-
  Run the match phase to find duplicate records within a single dataset using
  your trained Zingg model.
---

# Run the match phase

The `match` phase runs AFTER `train`. It applies the trained Zingg model to your full dataset and groups records that represent the same real-world entity into clusters.

Use `match` when you want to find duplicates within a single dataset. If you need to match records across two separate datasets, use the link phase instead - both are equal operations using the same trained model.

{% hint style="success" icon="right-long" %}
**Read more:**

* Scores are explained in detail - [Interpreting output scores](../interpreting-results/interpret-output-scores.md)
* For the link phase (across two datasets) - [Link across datasets](link-across-datasets.md)
{% endhint %}

{% tabs %}
{% tab title="Community" %}
### Python

```python
options = ClientOptions([
    ClientOptions.PHASE,
    "match"
])
zingg = Zingg(args, options)
zingg.initAndExecute()
```

### CLI

```bash
./scripts/zingg.sh --phase match --conf config.json
```

### Read and View Output

```python
output = spark.read.csv("path-to-output-directory",header = True)
display(output)
```

{% hint style="info" icon="right-long" %}
Matching records share the same `Z_CLUSTER` value.
{% endhint %}
{% endtab %}

{% tab title="Enterprise" %}
### Python

```python
options = ClientOptions([ClientOptions.PHASE,"match"])
zingg = EZingg(args, options)
zingg.initAndExecute()
```

### CLI

```bash
./scripts/zingg.sh --phase match --conf config.json
```

### Read and View Output

```python
# Read match output
output = spark.read.csv("path-to-output-directory",header=True)
display(output)
```

{% hint style="info" icon="right-long" %}
Enterprise output includes `Zingg ID` (persistent across runs) instead of `Z_CLUSTER`, plus deterministic match flag and Match Statistics. `Zingg ID` is stable across all subsequent incremental runs.
{% endhint %}
{% endtab %}

{% tab title="Enterprise Snowflake" %}

### Run match

Zingg on Snowflake can be run either from a local terminal via the CLI, or natively inside Snowflake as a job service.

#### CLI (local terminal)

```bash
./scripts/zingg.sh --phase match --conf config.json \
--properties-file <location to snowflake.properties>
```

#### Snowflake (job service)

Run the phase as an asynchronous job service inside your Snowflake compute pool.

```sql
EXECUTE JOB SERVICE
IN COMPUTE POOL CONTAINER_ZINGG_POOL
NAME = ZINGG_MATCH_ASYNC_JOB_SERVICE
ASYNC = true
EXTERNAL_ACCESS_INTEGRATIONS = (ALLOW_ALL_EAI)
FROM @specs SPECIFICATION_TEMPLATE_FILE = '<zingg-job-spec-file>'
USING (PHASE => 'match', CONFIG => '<config-name>');
```

`<zingg-job-spec-file>` defines the container and job service configuration Zingg needs to run inside Snowpark Container Services. It is maintained by your administrator and referenced from your `@specs` stage.

`<config-name>` is the name of your Zingg configuration json file.

**Monitor the job**

Retrieve the service logs to monitor execution and debug any failures.

```sql

SELECT SYSTEM$GET_SERVICE_LOGS('ZINGG_MATCH_ASYNC_JOB_SERVICE', 0, 'zingg-async-job-container');
```

`zingg-async-job-container` refers to the container name defined in the job specification.

**Stop the compute pool**

Once the job completes, stop the compute pool to release resources.

```sql
ALTER COMPUTE POOL CONTAINER_ZINGG_POOL STOP ALL;
```

{% hint style="info" icon="right-long" %}
Enterprise output includes `Zingg ID` (persistent across runs) instead of `Z_CLUSTER`, plus deterministic match flag and Match Statistics. `Zingg ID` is stable across all subsequent incremental runs.
{% endhint %}
{% endtab %}
{% endtabs %}