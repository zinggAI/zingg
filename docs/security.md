---
nav_order: 6
---

# Security And Privacy

Zingg models are built on your data and deployed within your network. No data leaves your environment.

However, Zingg does collect usage metrics and writes them to Google Analytics. This is done to understand and improve the user experience. Please be assured that **Zingg does not capture any user data or input data and will never do so.**

The following details are captured:

* **Data source type:** type of data format e.g. CSV, snowflake
* **Fields count:** number of fields used for training
* **Total Data count:** for match phase, number of total records
* **Execution Time:** execution time of the program
* **Matched and Nonmatched records count:** for the train phase, the number of matched and nonmatched records

If you do not wish to send this data, please set collectMetrics flag to false in the configuration JSON while running Zingg.

No usage data is being collected in the 0.3.0 release.
