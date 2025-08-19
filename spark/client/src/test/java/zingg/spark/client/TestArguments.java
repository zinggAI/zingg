package zingg.spark.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import zingg.common.client.arguments.ArgumentServiceImpl;
import zingg.common.client.arguments.IArgumentService;
import zingg.common.client.arguments.loader.LoaderFactory;
import zingg.common.client.arguments.model.Arguments;
import zingg.common.client.FieldDefinition;
import zingg.common.client.arguments.model.IArguments;
import zingg.common.client.IMatchType;
import zingg.common.client.MatchTypes;
import zingg.common.client.ZinggClientException;
import zingg.common.client.arguments.writer.WriterFactory;
import zingg.common.client.pipe.Pipe;
import zingg.spark.client.pipe.SparkPipe;

public class TestArguments {

	public static final Log LOG = LogFactory.getLog(TestArguments.class);
	protected final IArgumentService<Arguments> argumentService;

	public TestArguments() {
		this.argumentService = new ArgumentServiceImpl<>(Arguments.class, new LoaderFactory<>(),new WriterFactory<>());
	}
	@Test
	public void testWriteArgumentObjectToJSONFile() {
			IArguments args = new Arguments();
			try {
				FieldDefinition fname = new FieldDefinition();
				fname.setFieldName("fname");
				fname.setDataType("string");
				fname.setMatchType(Arrays.asList(MatchTypes.EXACT, MatchTypes.FUZZY, MatchTypes.PINCODE));
				//fname.setMatchType(Arrays.asList(MatchType.EXACT));
				fname.setFields("fname");
				FieldDefinition lname = new FieldDefinition();
				lname.setFieldName("lname");
				lname.setDataType("string");
				lname.setMatchType(Arrays.asList(MatchTypes.FUZZY));
				lname.setFields("lname");
				args.setFieldDefinition(Arrays.asList(fname, lname));

				Pipe inputPipe = new SparkPipe();
				inputPipe.setName("test");
				inputPipe.setFormat(Pipe.FORMAT_CSV);
				inputPipe.setProp("location", "examples/febrl/test.csv");
				args.setData(new Pipe[] {inputPipe});

				Pipe outputPipe = new SparkPipe();
				outputPipe.setName("output");
				outputPipe.setFormat(Pipe.FORMAT_CSV);
				outputPipe.setProp("location", "examples/febrl/output.csv");
				args.setOutput(new Pipe[] {outputPipe});

				args.setBlockSize(400L);
				args.setCollectMetrics(true);
				args.setModelId("500");
				argumentService.loadArguments("/tmp/configFromArgObject.json");

				//reload the same config file to check if deserialization is successful
				IArguments newArgs = argumentService.loadArguments("/tmp/configFromArgObject.json");
				assertEquals(newArgs.getModelId(), "500", "Model id is different");
				assertEquals(newArgs.getBlockSize(), 400L, "Block size is different");
				assertEquals(newArgs.getFieldDefinition().get(0).getFieldName(), "fname", "Field Definition[0]'s name is different");
				List<IMatchType> expectedMatchType = Arrays.asList(MatchTypes.EXACT, MatchTypes.FUZZY, MatchTypes.PINCODE);
				assertEquals(newArgs.getFieldDefinition().get(0).getMatchType(), expectedMatchType);
			} catch (Exception | ZinggClientException e) {
				e.printStackTrace();
			}
		}
}
