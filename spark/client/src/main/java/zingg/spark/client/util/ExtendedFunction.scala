package zingg.spark.client.util

import org.apache.spark.sql.functions.{col, collect_list, concat_ws}
import org.apache.spark.sql.{Dataset, Row}

class ExtendedFunction {
  def TransposeDF(df: Dataset[Row], columns: Seq[String], pivotCol: String): Dataset[Row] = {
    val columnsValue = columns.map(x => "'" + x + "', " + x)
    val stackCols = columnsValue.mkString(",")
    val df_1 = df.selectExpr(pivotCol, "stack(" + columns.size + "," + stackCols + ")")
      .select(pivotCol, "col0", "col1")

    val final_df = df_1.groupBy(col("col0")).pivot(pivotCol).agg(concat_ws("", collect_list(col("col1"))))
      .withColumnRenamed("col0", pivotCol)
    final_df
  }

}
