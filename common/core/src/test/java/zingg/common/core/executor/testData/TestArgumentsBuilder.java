package zingg.common.core.executor.testData;

import java.util.ArrayList;
import java.util.List;

import zingg.common.client.FieldDefinition;
import zingg.common.client.IMatchType;
import zingg.common.client.MatchTypes;
import zingg.common.client.ZinggClientException;
import zingg.common.client.arguments.model.Arguments;
import zingg.common.client.arguments.model.IArguments;
import zingg.common.client.pipe.FilePipe;
import zingg.common.client.pipe.Pipe;

/**
 * Builds the args the executor integration tests run on.
 *
 * These used to live in configSparkIntTest.json and configSparkLinkTest.json under
 * src/test/resources. Keeping them in code means the field definitions and schemas
 * are checked by the compiler, and a test can tweak one value without a second copy
 * of the whole config.
 *
 * The paths are passed in by the caller because the test data sits on the classpath -
 * only the driver knows where the class loader resolved it to.
 */
public class TestArgumentsBuilder {

	public static final String DELIMITER = ",";
	public static final String BAD_RECORDS_PATH = "/tmp/bad";
	public static final float LABEL_DATA_SAMPLE_SIZE = 0.5f;
	public static final int NUM_PARTITIONS = 4;

	/**
	 * Schema of test.csv - the record id followed by the ten compared fields.
	 */
	public static final String DATA_SCHEMA = "id string, fname string, lname string, stNo string, add1 string, add2 string, city string, state string, areacode string, dob string, ssn  string";

	/**
	 * Schema of training.csv - the two labelling columns followed by the record.
	 * areacode comes before state here, the other way round from DATA_SCHEMA, because
	 * that is the order the file itself is written in.
	 */
	public static final String TRAINING_SCHEMA = "z_cluster string, z_ismatch integer, id string, fname string, lname string, stNo string, add1 string, add2 string, city string, areacode string, state string, dob string, ssn  string";

	/**
	 * Schema of test1.csv and test2.csv - like DATA_SCHEMA but with areacode before state.
	 */
	public static final String LINK_DATA_SCHEMA = "id string, fname string, lname string, stNo string, add1 string, add2 string, city string, areacode string, state string, dob string, ssn  string";

	protected TestArgumentsBuilder() {
	}

	/**
	 * Args for the single and compound phases - one dataset, plus the training samples
	 * the labeller seeds itself from.
	 */
	public static IArguments buildSingleArgs(String modelId, String zinggDir, String dataPath,
			String trainingPath, String outputPath, String stopWordsPath) throws ZinggClientException {
		IArguments args = buildCommonArgs(modelId, zinggDir, outputPath);
		args.setData(new Pipe[] { csvPipe("test", dataPath, DATA_SCHEMA) });

		Pipe trainingPipe = csvPipe("trainingPos", trainingPath, TRAINING_SCHEMA);
		trainingPipe.setProp("badRecordsPath", BAD_RECORDS_PATH);
		args.setTrainingSamples(new Pipe[] { trainingPipe });

		args.setFieldDefinition(getMatchFieldDefinition(stopWordsPath));
		return args;
	}

	/**
	 * Args for the link phase - two datasets to link across, and no training samples,
	 * since linking runs off the model the earlier phases trained.
	 */
	public static IArguments buildLinkArgs(String modelId, String zinggDir, String data1Path,
			String data2Path, String outputPath) throws ZinggClientException {
		IArguments args = buildCommonArgs(modelId, zinggDir, outputPath);
		args.setData(new Pipe[] {
				csvPipe("test1", data1Path, LINK_DATA_SCHEMA),
				csvPipe("test2", data2Path, LINK_DATA_SCHEMA)
		});
		args.setFieldDefinition(getLinkFieldDefinition());
		return args;
	}

	protected static IArguments buildCommonArgs(String modelId, String zinggDir, String outputPath)
			throws ZinggClientException {
		IArguments args = new Arguments();
		args.setModelId(modelId);
		args.setZinggDir(zinggDir);
		args.setNumPartitions(NUM_PARTITIONS);
		args.setLabelDataSampleSize(LABEL_DATA_SAMPLE_SIZE);

		Pipe outputPipe = new Pipe();
		outputPipe.setName("output");
		outputPipe.setFormat(Pipe.FORMAT_CSV);
		outputPipe.setProp(FilePipe.PATH, outputPath);
		outputPipe.setProp(FilePipe.DELIMITER, DELIMITER);
		outputPipe.setProp(FilePipe.HEADER, "true");
		args.setOutput(new Pipe[] { outputPipe });

		return args;
	}

	/**
	 * id is carried through the output but not compared; everything else is fuzzy.
	 * add1 gets the stop word list so the stop word removal path is exercised too.
	 */
	protected static List<FieldDefinition> getMatchFieldDefinition(String stopWordsPath) {
		List<FieldDefinition> fieldDefinition = new ArrayList<FieldDefinition>();
		fieldDefinition.add(dontUseField("id"));
		fieldDefinition.add(fuzzyField("fname"));
		fieldDefinition.add(fuzzyField("lname"));
		fieldDefinition.add(fuzzyField("stNo"));

		FieldDefinition add1 = fuzzyField("add1");
		add1.setStopWords(stopWordsPath);
		fieldDefinition.add(add1);

		fieldDefinition.add(fuzzyField("add2"));
		fieldDefinition.add(fuzzyField("city"));
		fieldDefinition.add(fuzzyField("areacode"));
		fieldDefinition.add(fuzzyField("state"));
		fieldDefinition.add(fuzzyField("dob"));
		fieldDefinition.add(fuzzyField("ssn"));
		return fieldDefinition;
	}

	/**
	 * Linking compares the ten fields and leaves id out of the definition altogether.
	 */
	protected static List<FieldDefinition> getLinkFieldDefinition() {
		List<FieldDefinition> fieldDefinition = new ArrayList<FieldDefinition>();
		fieldDefinition.add(fuzzyField("fname"));
		fieldDefinition.add(fuzzyField("lname"));
		fieldDefinition.add(fuzzyField("stNo"));
		fieldDefinition.add(fuzzyField("add1"));
		fieldDefinition.add(fuzzyField("add2"));
		fieldDefinition.add(fuzzyField("city"));
		fieldDefinition.add(fuzzyField("areacode"));
		fieldDefinition.add(fuzzyField("state"));
		fieldDefinition.add(fuzzyField("dob"));
		fieldDefinition.add(fuzzyField("ssn"));
		return fieldDefinition;
	}

	protected static Pipe csvPipe(String name, String path, String schema) {
		Pipe pipe = new Pipe();
		pipe.setName(name);
		pipe.setFormat(Pipe.FORMAT_CSV);
		pipe.setProp(FilePipe.PATH, path);
		pipe.setProp(FilePipe.DELIMITER, DELIMITER);
		pipe.setProp(FilePipe.HEADER, "false");
		pipe.setSchema(schema);
		return pipe;
	}

	protected static FieldDefinition fuzzyField(String name) {
		return field(name, MatchTypes.FUZZY);
	}

	protected static FieldDefinition dontUseField(String name) {
		return field(name, MatchTypes.DONT_USE);
	}

	protected static FieldDefinition field(String name, IMatchType matchType) {
		FieldDefinition fieldDefinition = new FieldDefinition();
		fieldDefinition.setFieldName(name);
		fieldDefinition.setFields(name);
		fieldDefinition.setDataType("string");
		fieldDefinition.setMatchTypeInternal(matchType);
		return fieldDefinition;
	}

}
