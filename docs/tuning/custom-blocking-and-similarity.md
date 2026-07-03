---
description: >-
  Define your own blocking and similarity functions to replace or extend Zingg's
  built-in approaches for advanced accuracy tuning.
---

# Custom Blocking and Similarity

Zingg's built-in blocking and similarity functions handle most entity resolution use cases. For specialized data, such as company names with legal suffixes, phonetically similar names, or\
domain-specific identifiers; you can define your own functions.

Custom blocking functions control which records are compared. Custom similarity functions control how two field values are scored when they are compared.

### Custom blocking functions

Zingg evaluates custom blocking functions when building the blocking tree. The blocking tree works on the matched records you provided during labeling; at every node, Zingg selects the hash function and the field that produces the least elimination of your known matching pairs.

**What makes a good blocking function:**

A good blocking function never eliminates a matching pair entirely. It groups records that could be the same entity, even imperfectly; so the similarity model can evaluate them. A poor blocking function eliminates matching pairs from comparison entirely; no similarity scoring happens after that point.

**Example: evaluating `first1char` on `firstname`**

Take two labeled matching pairs:

| 1 | A | john  | j |
| - | - | ----- | - |
| 1 | B | johnh | j |
| 2 | A | mary  | m |
| 2 | B | marry | m |

Both pairs produce the same output from `first1char` - no elimination. This is a good function for `firstname`.

**Contrast: `last1char` on `firstname`**

| **1** | A | john  | n |
| ----- | - | ----- | - |
| 1     | B | johnh | h |
| 1     | A | mary  | y |
| 2     | B | marry | y |

Pair 1 is eliminated (`n` ≠ `h`). `last1char` is not a good function for `firstname`. Zingg will therefore not choose it.

So `first1char(firstname)` will be selected. It brings near-similar records together - clustering them to break the cartesian join.

#### Registering custom blocking functions

Business-specific blocking functions must be added to two places:

1. **`HashFunctionRegistry`** - registers the function so Zingg can find it
2. **hash functions config** - tells Zingg which functions are available to evaluate

Zingg evaluates all registered functions against your training data and selects the best combination automatically.

{% hint style="success" icon="right-long" %}
Custom blocking functions apply to both Community and Enterprise editions. The function evaluation happens during `train` . You do not need to re-run `findTrainingData` or `label` .
{% endhint %}

#### Custom similarity functions

For similarity, you can define your comparison measures alongside Zingg's built-in ones. Each `dataType` has predefined features—for example, the `string` type with `FUZZY` match is configured for Affine and Jaro-Winkler string comparison.

You can define your comparison functions and register them so Zingg uses them as additional features for the classifier. The classifier then determines the best weight for each feature, including your custom one, based on your labeled training data.

_**LINK TO BE ADDED -  GitHub Zingg repository custom similarity example (add link to****&#x20;****`github.com/zinggAI/zingg`****&#x20;****once confirmed by team)**_

Custom similarity and blocking function implementation requires Java/Scala code changes to the Zingg JAR. These are advanced customizations. For most use cases, adjusting match types, stopwords, and training data is sufficient before reaching this step.

