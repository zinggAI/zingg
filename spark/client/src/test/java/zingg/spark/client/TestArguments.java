package zingg.spark.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import zingg.common.client.Arguments;
import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchType;
import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.Pipe;
import zingg.spark.client.pipe.SparkPipe;

public class TestArguments {

	public static final Log LOG = LogFactory.getLog(TestArguments.class);

	@Test
	public void testWriteArgumentObjectToJSONFile() {
			Arguments args = new Arguments();
			try {
				FieldDefinition fname = new FieldDefinition();
				fname.setFieldName("fname");
				fname.setDataType("string");
				fname.setMatchType(Arrays.asList(MatchType.EXACT, MatchType.FUZZY, MatchType.PINCODE));
				//fname.setMatchType(Arrays.asList(MatchType.EXACT));
				fname.setFields("fname");
				FieldDefinition lname = new FieldDefinition();
				lname.setFieldName("lname");
				lname.setDataType("string");
				lname.setMatchType(Arrays.asList(MatchType.FUZZY));
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
                Arguments.writeArgumentsToJSON("/tmp/configFromArgObject.json", args);

				//reload the same config file to check if deserialization is successful
				Arguments newArgs = Arguments.createArgumentsFromJSON("/tmp/configFromArgObject.json", "test");
				assertEquals(newArgs.getModelId(), "500", "Model id is different");
				assertEquals(newArgs.getBlockSize(), 400L, "Block size is different");
				assertEquals(newArgs.getFieldDefinition().get(0).getFieldName(), "fname", "Field Definition[0]'s name is different");
				String expectedMatchType =  "[EXACT, FUZZY, PINCODE]";
				assertEquals(newArgs.getFieldDefinition().get(0).getMatchType().toString(), expectedMatchType);
			} catch (Exception | ZinggClientException e) {
				e.printStackTrace();
			}
		}
	}
