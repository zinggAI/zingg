package zingg.spark.connect;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
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
import zingg.common.client.*;
import zingg.common.client.Arguments;
import zingg.common.client.ClientOptions;
import zingg.spark.connect.*;
import zingg.spark.connect.proto.*;
import zingg.common.client.pipe.Pipe;
import zingg.spark.client.SparkClient;
import zingg.spark.client.pipe.SparkPipe;

import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.List;

public class ZinggConnectPlugin implements RelationPlugin {
    private SparkPipe parsePipe(zingg.spark.connect.proto.Pipe protoPipe) {
        SparkPipe sparkPipe = new SparkPipe();
        sparkPipe.setName(protoPipe.getName());

        // Parse DataFormat from proto
        DataFormat dataFormatProto = protoPipe.getFormat();
        if (dataFormatProto == DataFormat.DF_AVRO) {
            sparkPipe.setFormat(Pipe.FORMAT_AVRO);
        } else if (dataFormatProto == DataFormat.DF_BIGQUEY) {
            sparkPipe.setFormat(Pipe.FORMAT_BIGQUERY);
        } else if (dataFormatProto == DataFormat.DF_CASSANDRA) {
            sparkPipe.setFormat(Pipe.FORMAT_CASSANDRA);
        } else if (dataFormatProto == DataFormat.DF_CSV) {
            sparkPipe.setFormat(Pipe.FORMAT_CSV);
        } else if (dataFormatProto == DataFormat.DF_ELASTIC) {
            sparkPipe.setFormat(Pipe.FORMAT_ELASTIC);
        } else if (dataFormatProto == DataFormat.DF_EXACOL) {
            sparkPipe.setFormat(Pipe.FORMAT_EXASOL);
        } else if (dataFormatProto == DataFormat.DF_INMEMORY) {
            sparkPipe.setFormat(Pipe.FORMAT_INMEMORY);
        } else if (dataFormatProto == DataFormat.DF_JDBC) {
            sparkPipe.setFormat(Pipe.FORMAT_JDBC);
        } else if (dataFormatProto == DataFormat.DF_JSON) {
            sparkPipe.setFormat(Pipe.FORMAT_JSON);
        } else if (dataFormatProto == DataFormat.DF_PARQUET) {
            sparkPipe.setFormat(Pipe.FORMAT_PARQUET);
        } else if (dataFormatProto == DataFormat.DF_SNOWFLAKE) {
            sparkPipe.setFormat(Pipe.FORMAT_SNOWFLAKE);
        } else if (dataFormatProto == DataFormat.DF_TEXT) {
            sparkPipe.setFormat(Pipe.FORMAT_TEXT);
        } else if (dataFormatProto == DataFormat.DF_XLS) {
            sparkPipe.setFormat(Pipe.FORMAT_XLS);
        } else {
            throw new RuntimeException(String.format("Unknown format %s", dataFormatProto.name()));
        }

        // Parse tags
        for (Map.Entry<String, String> kv : protoPipe.getPropsMap().entrySet()) {
            sparkPipe.setProp(kv.getKey(), kv.getValue());
        }

        if (protoPipe.hasSchemaField()) {
            sparkPipe.setSchema(protoPipe.getSchemaField());
        }

        if (protoPipe.hasMode()) {
            sparkPipe.setMode(protoPipe.getMode());
        }

        return sparkPipe;
    }

    private SparkPipe[] parsePipes(List<zingg.spark.connect.proto.Pipe> protoPipes) {
        return protoPipes.stream().map(protoPipe -> parsePipe(protoPipe)).toArray(SparkPipe[]::new);
    }

    // 3.5.2 behaviour
    // Because of shading rules this method may be marked as wrongly overriden
    @Override
    public Option<LogicalPlan> transform(Any relation, SparkConnectPlanner planner) {
        if (relation.is(SubmitZinggJob.class)) {
            SubmitZinggJob zinggJobProto = relation.unpack(SubmitZinggJob.class);
            // It is expected that the session exisits!
            SparkSession spark = planner.sessionHolder().session();
            IArguments arguments = new Arguments();
            // Parse arguments

            // Output pipes
            arguments.setOutput(parsePipes(zinggJobProto.getArgumnets().getOutputList()));
            // Data pipes
            arguments.setData(parsePipes(zinggJobProto.getArgumnets().getDataList()));
            // Training samples
            arguments.setTrainingSamples(parsePipes(zinggJobProto.getArgumnets().getTrainingSamplesList()));

            // Arguments
            arguments.setZinggDir(zinggJobProto.getArgumnets().getZinggDir());
            arguments.setNumPartitions(zinggJobProto.getArgumnets().getNumPartitions());
            arguments.setLabelDataSampleSize(zinggJobProto.getArgumnets().getLabelDataSampleSize());
            arguments.setModelId(zinggJobProto.getArgumnets().getModelId());
            arguments.setThreshold(zinggJobProto.getArgumnets().getThreshold());
            arguments.setJobId(zinggJobProto.getArgumnets().getJobId());
            arguments.setCollectMetrics(zinggJobProto.getArgumnets().getCollectMetrics());
            arguments.setShowConcise(zinggJobProto.getArgumnets().getShowConcise());
            arguments.setStopWordsCutoff(zinggJobProto.getArgumnets().getStopWordsCutoff());
            arguments.setBlockSize(zinggJobProto.getArgumnets().getBlockSize());
            if (zinggJobProto.getArgumnets().hasColumn()) {
                arguments.setColumn(zinggJobProto.getArgumnets().getColumn());
            }

            // Options
            zingg.spark.connect.proto.ClientOptions clientOptionsProto = zinggJobProto.getCliOptions();
        }
    }

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
                                            new ArgumentsUtil().writeArgumentstoJSONString(client.getArguments()))),
                            new StructType(new StructField[] {
                                    DataTypes.createStructField("status", DataTypes.StringType, false),
                                    DataTypes.createStructField("newArgs", DataTypes.StringType, false)
                            }));
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
