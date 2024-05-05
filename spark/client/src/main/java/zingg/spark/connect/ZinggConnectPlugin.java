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
import zingg.common.client.*;
import zingg.spark.client.SparkClient;
import zingg.spark.connect.proto.SubmitZinggJob;

import java.util.Optional;

public class ZinggConnectPlugin implements RelationPlugin {
    @Override
    public Optional<LogicalPlan> transform(byte[] bytes, SparkConnectPlanner sparkConnectPlanner) {
        Any command;
        try {
            command = Any.parseFrom(bytes);
            if (!command.is(SubmitZinggJob.class)) {
                return Optional.empty();
            } else {
                try (SparkSession session = sparkConnectPlanner.sessionHolder().session()) {
                    SubmitZinggJob request = command.unpack(SubmitZinggJob.class);
                    String options = request.getOptions();
                    String args = request.getArgs();
                    ClientOptions clientOptions = new ClientOptions(options);
                    IArguments arguments = new ArgumentsUtil()
                            .createArgumentsFromJSONString(args, clientOptions.getOptionValue(ClientOptions.PHASE));
                    SparkClient client = new SparkClient(arguments, clientOptions, session);
                    client.init();
                    client.execute();
                    client.postMetrics();

                    Dataset<Row> outDF = session.createDataFrame(
                            List.of(
                                    RowFactory.create(
                                            "SUCCESS",
                                            new ArgumentsUtil().writeArgumentstoJSONString(client.getArguments())
                                    )
                            ),
                            new StructType(new StructField[]{
                                    DataTypes.createStructField("status", DataTypes.StringType, false),
                                    DataTypes.createStructField("newArgs", DataTypes.StringType, false)
                            })
                    );
                    return Optional.of(outDF.logicalPlan());
                }
            }
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Protobuf exception in SparkConnect", e);
        } catch (ZinggClientException e) {
            throw new RuntimeException("Zingg Internal Error", e);
        }
    }
}
