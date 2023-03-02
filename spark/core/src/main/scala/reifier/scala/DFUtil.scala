package zingg.scala

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.types.LongType
import org.apache.spark.sql.types.StructField
import zingg.common.client.util.ColName
import org.apache.spark.rdd.RDD
import org.graphframes.lib.AggregateMessages
import org.graphframes.GraphFrame
import org.apache.spark.sql.functions.{col, lit, sum, udf, when, min}

import java.util.HashMap;

object DFUtil {
  
  def addRowNumber(df: Dataset[Row], spark: SparkSession) : Dataset[Row] = {
    val schema = df.schema;
    val rows = df.rdd.zipWithUniqueId().map{case (r: Row, id: Long) => Row.fromSeq(id +: r.toSeq)}
    return spark.createDataFrame(rows, StructType(StructField(ColName.ID_COL, LongType, false) +: schema.fields))
    
    }
  
  
  def addClusterRowNumber(df: Dataset[Row], spark: SparkSession) : Dataset[Row] = {
    val schema = df.schema;
    val rows = df.rdd.zipWithUniqueId().map{case (r: Row, id: Long) => Row.fromSeq(id +: r.toSeq)}
    return spark.createDataFrame(rows, StructType(StructField(ColName.CLUSTER_COLUMN, LongType, false) +: schema.fields))
    
    }


  def scoring(df: Dataset[Row], edge: Dataset[Row]): Dataset[Row] = {
    val g: GraphFrame = GraphFrame(df, edge)

    // We will use AggregateMessages utilities later, so name it "AM" for short.
    val AM = AggregateMessages

    // For each user, sum the ages of the adjacent users.
    val msgToSrc = AM.dst("MSG")
    val msgToDst = AM.src("MSG")
    val agg = { g.aggregateMessages
      .sendToSrc(msgToSrc)  // send destination user's age to source
      .sendToDst(msgToDst)  // send source user's age to destination
      .agg(min(AM.msg).as("summedAges")) } // sum up ages, stored in AM.msg column
    agg.show()
    return agg
  }

/*
   def fit(dataset: Dataset[_], inputCol: String): RDD[(String, Long)] = {
    //transformSchema(dataset.schema, logging = true)
    val input = dataset.select(inputCol).rdd.map(_.getAs[Seq[String]](0))
   
    val wordCounts: RDD[(String, Long)] = input.flatMap { case (tokens) =>
      val wc = new HashMap[String, Long]
      tokens.foreach { w =>
        wc.changeValue(w, 1L, _ + 1L)
      }
      wc.map { case (word, count) => (word, (count, 1)) }
    }.reduceByKey { case ((wc1, df1), (wc2, df2)) =>
      (wc1 + wc2, df1 + df2)
    }.map { case (word, (count, dfCount)) =>
      (word, count)
    }.cache()
    
    wordCounts
  }
  */
  
  
}
