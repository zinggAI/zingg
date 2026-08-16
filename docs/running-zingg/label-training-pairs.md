---
description: >-
  Review candidate pairs and label each as Match, No Match, or Can't Say. The
  human feedback step that teaches Zingg what a match looks like in your data.
---

# Label Training Pairs

The `label` phase opens an interactive layer where you review the candidate pairs found by `findTrainingData` and mark each pair. This is the only step in the Zingg workflow that requires human input. No ML knowledge is needed, just your domain understanding of whether two records represent the same real-world entity.

If you already have labeled data from an external source, you can supply it directly using `trainingSamples` in your configuration. See [Create Training Data](create-training-data.md) for how to set this up.

30 to 40 matching pairs is a strong starting point. Label until you feel that your labeled examples represent all field types and data patterns in your schema. If accuracy needs improvement after your first match run, return to labeling—focus on the patterns or field combinations that appear to be missing or underrepresented.

{% stepper %}
{% step %}
### Step 1: Label

Review each pair presented. Enter your decision: Match (1), No Match (2), or Can't Say (0). You will see different attribute variations at each stage.
{% endstep %}

{% step %}
### Step 2: Save

Your labels are saved automatically to `zinggDir/modelId` after each session.
{% endstep %}

{% step %}
### Step 3: Iterate

Run `findTrainingData` again to obtain a fresh set of candidate pairs. Label those. Repeat until Zingg's predictions align with your expectations.
{% endstep %}
{% endstepper %}

{% tabs %}
{% tab title="Community" %}
In Community, pairs are presented serially in the terminal. Review each pair and enter your decision.

### **Python**

```python
options = ClientOptions([
    ClientOptions.PHASE,
    "label"
])
zingg = Zingg(args, options)
zingg.initAndExecute()
```

### **CLI**

```bash
./scripts/zingg.sh --phase label --conf config.json --showConcise=true
```

{% hint style="success" icon="right-long" %}
`--showConcise=true` is optional. It only shows fields which are `NOT DONT_USE`, making the labelling session cleaner when you have many fields.
{% endhint %}

### `updateLabel` section

As your understanding of your data evolves, you may need to revisit and correct previously marked pairs. Generate model documentation first (see \[Generate Model Documentation]), then run `updateLabel`:

#### Python

```python
options = ClientOptions([
    ClientOptions.PHASE,
    "updateLabel"
])
zingg = Zingg(args, options)
zingg.initAndExecute()
```

#### CLI

```bash
./scripts/zingg.sh --phase updateLabel --conf <location to conf.json>
```

This opens the console labeler, which accepts the cluster ID of the pairs you want to update. Note: Keep a backup of your model folder before running `updateLabel`.
{% endtab %}

{% tab title="Enterprise" %}
`--showConcise=true` is optional. It only shows fields which are `NOT DONT_USE`, making the labelling session cleaner when you have many fields.

#### Python

```python
options = ClientOptions([
    ClientOptions.PHASE,
    "label"
])
zingg = EZingg(args, options)
zingg.initAndExecute()
```

{% hint style="info" icon="right-long" %}
Enterprise provides a visual widget showing one pair at a time with Match, No Match, and Can't Say buttons. Download the diagnostics view to share match quality with stakeholders before committing to training.
{% endhint %}

{% endtab %}

{% tab title="Enterprise Snowflake" %}
`--showConcise=true` is optional. It only shows fields which are `NOT DONT_USE`, making the labelling session cleaner when you have many fields.

### Run label

Zingg on Snowflake can be run either from a local terminal via the CLI, or natively inside Snowflake using an interactive labeling service.

#### CLI (local terminal)

```bash
./scripts/zingg.sh --phase label --conf config.json --showConcise=true \
--properties-file <location to snowflake.properties>
```

#### Snowflake (interactive service)

The `label` phase is interactive, so instead of an async job, it runs as a standing service you connect to directly.

```sql
CREATE SERVICE CONTAINER_ZINGG_DB.PUBLIC.ZINGG_CLI_SERVICE
IN COMPUTE POOL CONTAINER_ZINGG_POOL
FROM @specs
SPECIFICATION_FILE = '<zingg-cli-spec-file>'
EXTERNAL_ACCESS_INTEGRATIONS = (ALLOW_ALL_EAI);
```

`<zingg-cli-spec-file>` is the specification file that defines the container and service configuration used to run the Zingg CLI interactively inside Snowpark Container Services. It is maintained by your administrator and referenced from your `@specs` stage.

Once the service is running, retrieve its endpoint to connect to the labeling session.

```sql
SHOW ENDPOINTS IN SERVICE ZINGG_CLI_SERVICE;
```

Use the returned endpoint to open the interactive labeling terminal and label pairs as you would via CLI.

**Stop the compute pool**

Once you're done labeling, stop the compute pool to release resources.

```sql
ALTER COMPUTE POOL CONTAINER_ZINGG_POOL STOP ALL;
```

{% hint style="info" icon="right-long" %}
Enterprise provides a visual widget showing one pair at a time with Match, No Match, and Can't Say buttons. Download the diagnostics view to share match quality with stakeholders before committing to training.
{% endhint %}
{% endtab %}
{% endtabs %}