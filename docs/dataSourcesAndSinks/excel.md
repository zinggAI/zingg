---
layout: default
title: Excel (XLS/XLSX)
parent: Data Sources and Sinks
nav_order: 12
---

# Excel (XLS/XLSX)

Zingg can read and write Microsoft Excel files (`.xls` and `.xlsx`) using the
[spark-excel](https://github.com/crealytics/spark-excel) connector. As Spark does
not support Excel natively, the connector jar has to be added to the classpath at
runtime.

## Adding the spark-excel jar

`spark-excel` is not bundled with Zingg. Pick the artifact that matches your Spark
and Scala versions — the coordinate is
`com.crealytics:spark-excel_<scalaVersion>:<sparkVersion>_<connectorVersion>`.
For Spark 3.5.x with Scala 2.12 this is
`com.crealytics:spark-excel_2.12:3.5.1_0.20.4`.

Add it in one of two ways:

1. **Through `zingg.conf`** — download the jar (and its transitive dependencies)
   and list them under `spark.jars` in your
   [runtime properties](../stepbystep/zingg-runtime-properties.md) file:

   ```properties
   spark.jars=/path/to/spark-excel_2.12-3.5.1_0.20.4.jar
   ```

2. **Through `spark.jars.packages`** — let Spark resolve the connector and its
   dependencies from Maven automatically:

   ```properties
   spark.jars.packages=com.crealytics:spark-excel_2.12:3.5.1_0.20.4
   ```

## Reading Excel as a source

```json
"data" : [{
		"name":"test",
		"format":"com.crealytics.spark.excel",
		"props": {
			"location": "examples/febrl/test.xlsx",
			"header": "true",
			"dataAddress": "'febrl'!A1"
		},
		"schema": "recId string, fname string, lname string, stNo string, add1 string, add2 string, city string, areacode string, state string, dob string, ssn string"
	}]
```

## Writing Excel as a sink

```json
"output" : [{
		"name":"output",
		"format":"com.crealytics.spark.excel",
		"props": {
			"location": "/tmp/zinggOutput.xlsx",
			"header": "true",
			"dataAddress": "'output'!A1"
		}
	}]
```

## Useful options

The values in `props` are passed straight through to spark-excel. Commonly used ones:

| Option | Description |
| --- | --- |
| `header` | `true` if the first row holds column names. |
| `dataAddress` | Sheet/cell range to read or write, e.g. `'febrl'!A1`. Defaults to the first sheet. |
| `inferSchema` | Infer column types when no `schema` is supplied. Prefer supplying an explicit `schema`. |
| `usePlainNumberFormat` | Read numeric cells without scientific/locale formatting. |

Both `.xls` and `.xlsx` are read and written with the same
`com.crealytics.spark.excel` format string; the file extension in `location`
determines the workbook format on write.

A ready-to-run example config is at
[`examples/febrl/configExcel.json`](https://github.com/zinggAI/zingg/blob/main/examples/febrl/configExcel.json).
