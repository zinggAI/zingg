package zingg.spark.connect.server;

import java.util.ArrayList;
import java.util.List;

import zingg.common.client.ClientOptions;
import zingg.common.client.FieldDefinition;
import zingg.common.client.IMatchType;
import zingg.common.client.MatchTypes;
import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.Pipe;
import zingg.spark.client.pipe.SparkPipe;

/**
 * Converts between the wire types generated from zingg_command.proto and the
 * existing common/client Java model (Arguments, FieldDefinition, Pipe,
 * ClientOptions). Every field is copied explicitly by name/setter, never
 * positionally, so adding or removing a proto field later only requires a
 * change here -- it can never silently shift the meaning of an existing one.
 */
public class ZinggProtoConverters {

	private ZinggProtoConverters() {
	}

	public static SparkPipe toJavaPipe(zingg.spark.connect.proto.Pipe p) {
		SparkPipe pipe = new SparkPipe();
		pipe.setName(p.getName());
		pipe.setFormat(p.getFormat());
		if (!p.getPreprocessors().isEmpty()) {
			pipe.setPreprocessors(p.getPreprocessors());
		}
		for (java.util.Map.Entry<String, String> e : p.getPropsMap().entrySet()) {
			pipe.setProp(e.getKey(), e.getValue());
		}
		if (!p.getSchema().isEmpty()) {
			pipe.setSchema(p.getSchema());
		}
		if (!p.getMode().isEmpty()) {
			pipe.setMode(p.getMode());
		}
		pipe.setId(p.getId());
		return pipe;
	}

	public static Pipe[] toJavaPipes(List<zingg.spark.connect.proto.Pipe> pipes) {
		Pipe[] result = new Pipe[pipes.size()];
		for (int i = 0; i < pipes.size(); i++) {
			result[i] = toJavaPipe(pipes.get(i));
		}
		return result;
	}

	public static FieldDefinition toJavaFieldDefinition(zingg.spark.connect.proto.FieldDefinition fd) {
		FieldDefinition javaFd = new FieldDefinition();
		javaFd.setFieldName(fd.getFieldName());
		javaFd.setDataType(fd.getDataType());
		List<IMatchType> matchTypes = new ArrayList<>();
		for (zingg.spark.connect.proto.MatchType mt : fd.getMatchTypeList()) {
			matchTypes.add(MatchTypes.getByName(mt.getName()));
		}
		javaFd.setMatchType(matchTypes);
		if (!fd.getFields().isEmpty()) {
			javaFd.setFields(fd.getFields());
		} else {
			javaFd.setFields(fd.getFieldName());
		}
		if (!fd.getStopWords().isEmpty()) {
			javaFd.setStopWords(fd.getStopWords());
		}
		if (!fd.getAbbreviations().isEmpty()) {
			javaFd.setAbbreviations(fd.getAbbreviations());
		}
		return javaFd;
	}

	public static zingg.common.client.arguments.model.Arguments toJavaArguments(zingg.spark.connect.proto.Arguments a)
			throws ZinggClientException {
		zingg.common.client.arguments.model.Arguments args = new zingg.common.client.arguments.model.Arguments();

		if (!a.getZinggDir().isEmpty()) {
			args.setZinggDir(a.getZinggDir());
		}
		if (!a.getModelId().isEmpty()) {
			args.setModelId(a.getModelId());
		}
		if (a.getJobId() != 0) {
			args.setJobId(a.getJobId());
		}
		args.setCollectMetrics(a.getCollectMetrics());
		if (a.getOutputCount() > 0) {
			args.setOutput(toJavaPipes(a.getOutputList()));
		}
		if (a.getDataCount() > 0) {
			args.setData(toJavaPipes(a.getDataList()));
		}
		if (a.getTrainingSamplesCount() > 0) {
			args.setTrainingSamples(toJavaPipes(a.getTrainingSamplesList()));
		}
		if (a.getFieldDefinitionCount() > 0) {
			List<FieldDefinition> fieldDefs = new ArrayList<>();
			for (zingg.spark.connect.proto.FieldDefinition fd : a.getFieldDefinitionList()) {
				fieldDefs.add(toJavaFieldDefinition(fd));
			}
			args.setFieldDefinition(fieldDefs);
		}
		if (a.getNumPartitions() != 0) {
			args.setNumPartitions(a.getNumPartitions());
		}
		if (a.getLabelDataSampleSize() != 0) {
			args.setLabelDataSampleSize(a.getLabelDataSampleSize());
		}
		if (a.getThreshold() != 0) {
			args.setThreshold(a.getThreshold());
		}
		args.setShowConcise(a.getShowConcise());
		if (a.getStopWordsCutoff() != 0) {
			args.setStopWordsCutoff(a.getStopWordsCutoff());
		}
		if (a.getBlockSize() != 0) {
			args.setBlockSize(a.getBlockSize());
		}
		if (!a.getColumn().isEmpty()) {
			args.setColumn(a.getColumn());
		}
		return args;
	}

	/**
	 * ClientOptions has no proto-friendly constructor of its own -- it is
	 * built from a CLI-style string list and requires --phase/--license/--conf
	 * to be present (see zingg.common.client.ClientOptions#loadOptions). We
	 * synthesize that list the same way python/zingg/client.py's
	 * ClientOptions.__init__ already does today: fill placeholders for
	 * --license/--conf since a Connect invocation carries structured
	 * Arguments, not a JSON config file path.
	 */
	public static ClientOptions toJavaClientOptions(String phase, zingg.spark.connect.proto.ClientOptions options) {
		List<String> cliArgs = new ArrayList<>();
		cliArgs.add(ClientOptions.PHASE);
		cliArgs.add(phase);
		cliArgs.add(ClientOptions.LICENSE);
		cliArgs.add(!options.getLicense().isEmpty() ? options.getLicense() : "zinggLic.txt");
		cliArgs.add(ClientOptions.EMAIL);
		cliArgs.add(!options.getEmail().isEmpty() ? options.getEmail() : "zingg@zingg.ai");
		cliArgs.add(ClientOptions.CONF);
		cliArgs.add("dummyConf.json");
		if (!options.getJobId().isEmpty()) {
			cliArgs.add(ClientOptions.JOBID);
			cliArgs.add(options.getJobId());
		}
		if (!options.getFormat().isEmpty()) {
			cliArgs.add(ClientOptions.FORMAT);
			cliArgs.add(options.getFormat());
		}
		if (!options.getLocation().isEmpty()) {
			cliArgs.add(ClientOptions.LOCATION);
			cliArgs.add(options.getLocation());
		}
		if (!options.getColumn().isEmpty()) {
			cliArgs.add(ClientOptions.COLUMN);
			cliArgs.add(options.getColumn());
		}
		return new ClientOptions(cliArgs);
	}
}
