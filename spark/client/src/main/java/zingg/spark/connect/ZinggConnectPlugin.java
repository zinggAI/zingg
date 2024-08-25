package zingg.spark.connect;

import java.util.List;
import java.util.Map;

import com.google.protobuf.Any;

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
import zingg.common.client.Arguments;
import zingg.common.client.ArgumentsUtil;
import zingg.common.client.ClientOptions;
import zingg.common.client.FieldDefinition;
import zingg.common.client.IArguments;
import zingg.common.client.pipe.Pipe;
import zingg.spark.client.pipe.SparkPipe;
import zingg.spark.connect.proto.DataFormat;
import zingg.spark.connect.proto.MatchType;
import zingg.spark.connect.proto.SubmitZinggJob;

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

    private FieldDefinition parseFieldDefinition(zingg.spark.connect.proto.FieldDefinition fieldDefinitionProto) {
        FieldDefinition fieldDefinition = new FieldDefinition();
        fieldDefinition.setMatchType(fieldDefinitionProto.getMatchTypeList().stream().map(mt -> {
            if (mt == MatchType.MT_FUZZY) {
                return zingg.common.client.MatchType.FUZZY;
            } else if (mt == MatchType.MT_EXACT) {
                return zingg.common.client.MatchType.EXACT;
            } else if (mt == MatchType.MT_DONT_USE) {
                return zingg.common.client.MatchType.DONT_USE;
            } else if (mt == MatchType.MT_EMAIL) {
                return zingg.common.client.MatchType.EMAIL;
            } else if (mt == MatchType.MT_PINCODE) {
                return zingg.common.client.MatchType.PINCODE;
            } else if (mt == MatchType.MT_NULL_OR_BLANK) {
                return zingg.common.client.MatchType.NULL_OR_BLANK;
            } else if (mt == MatchType.MT_TEXT) {
                return zingg.common.client.MatchType.TEXT;
            } else if (mt == MatchType.MT_NUMERIC) {
                return zingg.common.client.MatchType.NUMERIC;
            } else if (mt == MatchType.MT_NUMERIC_WITH_UNITS) {
                return zingg.common.client.MatchType.NUMERIC_WITH_UNITS;
            } else if (mt == MatchType.MT_ONLY_ALPHABETS_EXACT) {
                return zingg.common.client.MatchType.ONLY_ALPHABETS_EXACT;
            } else if (mt == MatchType.MT_ONLY_ALPHABETS_FUZZY) {
                return zingg.common.client.MatchType.ONLY_ALPHABETS_FUZZY;
            } else {
                throw new RuntimeException(String.format("Unknown type %s", mt.name()));
            }
        }).toList());

        fieldDefinition.setDataType(fieldDefinitionProto.getDataType());
        fieldDefinition.setFieldName(fieldDefinitionProto.getFieldName());
        fieldDefinition.setFields(fieldDefinitionProto.getFields());
        if (fieldDefinitionProto.hasStopWords()) {
            fieldDefinition.setStopWords(fieldDefinitionProto.getStopWords());
        }
        if (fieldDefinitionProto.hasAbbreviations()) {
            fieldDefinition.setAbbreviations(fieldDefinitionProto.getAbbreviations());
        }

        return fieldDefinition;
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
            // Field definitions
            arguments.setFieldDefinition(zinggJobProto.getArgumnets().getFiieldDefinitionList().stream()
                    .map(fd -> parseFieldDefinition(fd)).toList());

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
            ClientOptions clientOptions = new ClientOptions();

            if (clientOptionsProto.hasPhase()) {
                clientOptions.setOptionValue(ClientOptions.PHASE, clientOptionsProto.getPhase());
            }
            if (clientOptionsProto.hasLicense()) {
                clientOptions.setOptionValue(ClientOptions.LICENSE, clientOptionsProto.getLicense());
            }
            if (clientOptionsProto.hasEmail()) {
                clientOptions.setOptionValue(ClientOptions.EMAIL, clientOptionsProto.getEmail());
            }
            if (clientOptionsProto.hasConf()) {
                clientOptions.setOptionValue(ClientOptions.CONF, clientOptionsProto.getConf());
            }
            if (clientOptionsProto.hasPreprocess()) {
                clientOptions.setOptionValue(ClientOptions.PREPROCESS, clientOptionsProto.getPreprocess());
            }
            if (clientOptionsProto.hasJobId()) {
                clientOptions.setOptionValue(ClientOptions.JOBID, clientOptionsProto.getJobId());
            }
            if (clientOptionsProto.hasFormat()) {
                clientOptions.setOptionValue(ClientOptions.FORMAT, clientOptionsProto.getFormat());
            }
            if (clientOptionsProto.hasZinggDir()) {
                clientOptions.setOptionValue(ClientOptions.ZINGG_DIR, clientOptionsProto.getZinggDir());
            }
            if (clientOptionsProto.hasModelId()) {
                clientOptions.setOptionValue(ClientOptions.MODEL_ID, clientOptionsProto.getModelId());
            }
            if (clientOptionsProto.hasCollectMetrics()) {
                clientOptions.setOptionValue(ClientOptions.COLLECT_METRICS, clientOptionsProto.getCollectMetrics());
            }
            if (clientOptionsProto.hasShowConcise()) {
                clientOptions.setOptionValue(ClientOptions.SHOW_CONCISE, clientOptionsProto.getShowConcise());
            }
            if (clientOptionsProto.hasLocation()) {
                clientOptions.setOptionValue(ClientOptions.LOCATION, clientOptionsProto.getLocation());
            }
            if (clientOptionsProto.hasColumn()) {
                clientOptions.setOptionValue(ClientOptions.COLUMN, clientOptionsProto.getColumn());
            }
            if (clientOptionsProto.hasRemote()) {
                clientOptions.setOptionValue(ClientOptions.REMOTE, clientOptionsProto.getRemote());
            }

            Dataset<Row> outDF = spark.createDataFrame(
                    List.of(RowFactory.create(new ArgumentsUtil().writeArgumentstoJSONString(arguments),
                            String.join(" ", clientOptions.getCommandLineArgs()))),
                    new StructType(new StructField[] { DataTypes.createStructField("args", DataTypes.StringType, false),
                            DataTypes.createStructField("cliopts", DataTypes.StringType, false) }));
            return Option.apply(outDF.logicalPlan());
        }
    }
}
