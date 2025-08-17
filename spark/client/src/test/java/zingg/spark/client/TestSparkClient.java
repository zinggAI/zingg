package zingg.spark.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import zingg.common.client.arguments.model.Arguments;
import zingg.common.client.ClientOptions;
import zingg.common.client.arguments.model.IArguments;
import zingg.common.client.arguments.model.IZArgs;
import zingg.common.client.ArgumentsAssembler;
import zingg.common.client.BannerPrinter;

public class TestSparkClient {

	@Test
	public void testSetColumnOptionThroughBuildAndSetArguments() {
		IArguments arguments = new Arguments();
		String[] args = {ClientOptions.CONF, "configFile", ClientOptions.PHASE, "train", ClientOptions.COLUMN, "columnName", ClientOptions.SHOW_CONCISE, "true", ClientOptions.LICENSE, "licenseFile"};
		ClientOptions options = new ClientOptions(args);
		ArgumentsAssembler argumentsAssembler = new ArgumentsAssembler();
		IZArgs assembledArguments = argumentsAssembler.assemble(arguments, options);
		SparkClient client = new SparkClient(new BannerPrinter());
		client.setArguments(assembledArguments);
		client.setOptions(options);

		assertEquals("columnName", ((IArguments)client.getArguments()).getColumn());
	}

}
