package zingg.spark.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import zingg.common.client.Arguments;
import zingg.common.client.Client;
import zingg.common.client.ClientOptions;
import zingg.common.client.IArguments;

public class TestSparkClient {

	@Test
	public void testSetColumnOptionThroughBuildAndSetArguments() {
		IArguments arguments = new Arguments();
		String[] args = {ClientOptions.CONF, "configFile", ClientOptions.PHASE, "train", ClientOptions.COLUMN, "columnName", ClientOptions.SHOW_CONCISE, "true", ClientOptions.LICENSE, "licenseFile"};
		ClientOptions options = new ClientOptions(args);
		Client client = new SparkClient();
		client.buildAndSetArguments(arguments, options);

		assertEquals("columnName", ((IArguments)client.getArguments()).getColumn());
	}

	
}
