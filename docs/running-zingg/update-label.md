---
description: >-
  Revisit and correct a previously labeled pair by cluster ID, without
  re-labeling everything from scratch.
---

# 🧪 Update Label

{% hint style="warning" icon="triangle-exclamation" %}
Keep a backup of your model folder before running `updateLabel` - it overwrites previously saved labels.
{% endhint %}

As your understanding of your data evolves, you may need to correct a pair you labeled earlier. The `updateLabel` phase reopens the console labeler for one cluster at a time, instead of stepping through every candidate pair again.

Generate model documentation first - see [Generate Model Documentation](generate-model-documentation.md) - so you can look up the cluster ID of the pair you want to correct.

### Run updateLabel

#### Python

```python
options = ClientOptions([ClientOptions.PHASE, "updateLabel"])
zingg = Zingg(args, options)
zingg.initAndExecute()
```

#### CLI

```bash
./scripts/zingg.sh --phase updateLabel --conf <location to conf.json>
```

### What happens next

The command opens an interactive prompt in the terminal. Enter the cluster ID of the pair you want to relabel, review the pair it displays, then enter the corrected decision: Match (1), No Match (2), or Can't Say (0). Enter 9 at either prompt to exit.

{% hint style="success" icon="check-circle" %}
After updating labels, run [Generate Model Documentation](generate-model-documentation.md) again to confirm the change, then re-run `train` once you're satisfied.
{% endhint %}
