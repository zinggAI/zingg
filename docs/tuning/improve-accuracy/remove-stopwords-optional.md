---
description: >-
  An optional pre-processing step that improves matching accuracy by excluding
  high-frequency words that carry no matching signal.
---

# Remove Stopwords (Optional)

{% hint style="success" icon="right-long" %}
This is an optional step. Run this between [Configure Zingg](../../running-zingg/configure-zingg.md) and [Create Training Data](../../running-zingg/create-training-data.md) for best results. You can run it after matching has already started, but you will need to re-run the match phase for the stopwords to take effect. Most users skip this on the first run.
{% endhint %}

Common words like `Mr`, `Pvt`, `Av`, `St`, `Street` do not add differential signals and can confuse matching. These are called stopwords. Zingg can recommend which words to treat as stopwords by analyzing your data before training begins.

This step is optional but recommended for address, company name, and location\
fields where generic terms are common.

{% tabs %}
{% tab title="Community" %}
### **Step 1: Generate stopword recommendations**

#### **Python**

```python
options = ClientOptions([ ClientOptions.PHASE, "recommend" ])
zingg = Zingg(args, options)
zingg.setColumn("<column_name>")
zingg.initAndExecute()
```

#### **CLI**

```bash
./scripts/zingg.sh --phase recommend --conf config.json --column <column_name>
```

{% hint style="success" icon="right-long" %}
Stopwords are stored at: `models/100/stopWords/columnName`. The output gives you the list of stopwords along with their frequency.
{% endhint %}

### **Step 2: Review the recommendations**

By default, Zingg extracts 10% of the high-frequency unique words from your dataset. To change this, set `stopWordsCutoff` in your config file under the relevant field: `"stopWordsCutoff": 0.1`.

### **Step 3: Configure stopwords in your field definition**

Once you have verified the stopwords, add the `stopWords` path to the relevant field in your config. The CSV file must have one word per row and include a header row - Zingg ignores the header by default.

#### **JSON**

```json
{
  "fieldDefinition": [{
    "fieldName": "fname",
    "matchType": "fuzzy",
    "fields": "fname",
    "dataType": "string",
    "stopWords": "models/100/stopWords/fname.csv"
  }]
}
```
{% endtab %}

{% tab title="Enterprise" %}
### **Python**

```python
options = ClientOptions([ ClientOptions.PHASE, "recommend" ])
zingg = EZingg(args, options)
zingg.setColumn("<column_name>")
zingg.initAndExecute()
```

### **CLI**

```bash
./scripts/zingg.sh --phase recommend --conf config.json --column <column_name>
```
{% endtab %}

{% tab title="Enterprise Snowflake" %}
Stopwords are stored as a table: `zingg_stopWords_columnName_modelId`

**CHECK WITH SONAL ABOUT THIS TOPIC - NEEDS ENTIRELY DIFFERENT SET OF CONTENT TO BE DISCUSSED LATER.**
{% endtab %}
{% endtabs %}
