package zingg.spark.connect;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.sun.tools.javac.util.List;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan;
import org.apache.spark.sql.connect.planner.SparkConnectPlanner;
import org.apache.spark.sql.connect.plugin.RelationPlugin;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.Option;
import zingg.common.client.ArgumentsUtil;
import zingg.common.client.ClientOptions;
import zingg.common.client.IArguments;
import zingg.common.client.ZinggClientException;
import zingg.spark.client.SparkClient;
import zingg.spark.connect.proto.SubmitZinggJob;

class ZinggConnectPlugin implements RelationPlugin {
	@Override
	public Option<LogicalPlan> transform(org.sparkproject.connect.protobuf.Any relation, SparkConnectPlanner planner) {
        if (relation.is(SubmitZinggJob.class)) {
            SubmitZinggJob message;
            try {
                message = relation.unpack(SubmitZinggJob.class);
            } catch (InvalidProtocolBufferException e) {
                // Should be unreachable due to explicit check of the message type
                throw new RuntimeException(e);
            }
            String cliArgs = message.getArgs();
            String options = message.getOptions();
            // Parsing of options and arguments
            ClientOptions clientOptions = new ClientOptions(cliArgs);
            IArguments arguments;
            try {
                arguments = new ArgumentsUtil().createArgumentsFromJSONString(
                        options,
                        clientOptions.getOptionValue(ClientOptions.PHASE)
                );
            } catch (ZinggClientException e) {
                throw new RuntimeException(e);
            }
            // Get active session and create Zingg Client
            try (SparkSession session = planner.sessionHolder().session()) {
                SparkClient client = new SparkClient(arguments, clientOptions, session);
                // TODO: How to capture logs to send them back to client?

                // Run the job
                client.init();
                client.execute();
                client.postMetrics();

                // Build an output DataFrame object
                IArguments newArgs = client.getArguments();
                Dataset<Row> outputDf = session.createDataFrame(
                        List.of(RowFactory.create("SUCESS", new ArgumentsUtil().writeArgumentstoJSONString(newArgs))),
                        new StructType(
                                new StructField[]{
                                        DataTypes.createStructField("status", DataTypes.StringType, false),
                                        DataTypes.createStructField("newArgs", DataTypes.StringType, false)
                                }
                        )
                );
                return Option.apply(outputDf.logicalPlan());
            } catch (ZinggClientException e) {
                throw new RuntimeException(e);
            }
        }
        return Option.empty();
	}
}
