# Updating Labeled Pairs

N**ote: Please keep a backup copy of your model folder in a separate place before running this**

As our understanding of our data changes, we may need to revisit the previously marked pairs and update them. To do this, please [generate the documentation of the model.](generatingdocumentation.md)

You can then invoke the updater by invoking\
`./scripts/zingg.sh --phase updateLabel --conf <location to conf.json>`

This brings up the console labeler which accepts the **cluster id** of the pairs you want to update.

![Shows records and asks user to update yes, no, can't say on the CLI.](../assets/update.gif)
