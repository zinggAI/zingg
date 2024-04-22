package zingg.spark.connect

import com.google.protobuf
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.connect.planner.SparkConnectPlanner
import org.apache.spark.sql.connect.plugin.RelationPlugin
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import zingg.common.client.{ArgumentsUtil, ClientOptions}
import zingg.spark.client.SparkClient
import zingg.spark.connect.proto.SubmitZinggJob

import scala.collection.JavaConversions._

class ZinggConnectPlugin extends RelationPlugin {
  override def transform(relation: protobuf.Any, planner: SparkConnectPlanner): Option[LogicalPlan] = {
    if (!relation.is(classOf[SubmitZinggJob])) {
      Option.empty
    } else {
      val message = relation.unpack(classOf[SubmitZinggJob])
      val spark = planner.sessionHolder.session
      val options = new ClientOptions(message.getOptions)
      val args = new ArgumentsUtil().createArgumentsFromJSONString(message.getArgs, options.getOptionValue(ClientOptions.PHASE))
      val client = new SparkClient(args, options, spark)
      client.init()
      client.execute()
      client.postMetrics()

      val outDf = spark.createDataFrame(
        Seq(Row("SUCEESS", new ArgumentsUtil().writeArgumentstoJSONString(client.getArguments))),
        StructType(
          Seq(
            StructField("status", StringType, nullable = false),
            StructField("newArgs", StringType, nullable = false)
          )
        )
      )

      Option(outDf.queryExecution.logical)
    }
  }
}
