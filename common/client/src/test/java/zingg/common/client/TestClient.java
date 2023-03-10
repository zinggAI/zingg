package zingg.common.client;

import static org.junit.jupiter.api.Assertions.fail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

public class TestClient {
	public static final Log LOG = LogFactory.getLog(TestClient.class);

	@Test
	public void testValidPhase() {
		String phase = "train";
		try {
			ZinggOptions.verifyPhase(phase);
		} catch (ZinggClientException e1) {
			fail("No exception was expected as it is a valid phase: " + phase);
		}
	}

	@Test
	public void testInvalidPhase() {
		String phase = "tain";
		try {
			ZinggOptions.verifyPhase(phase);
			fail("An exception should have been thrown for an invalid phase");
		} catch (ZinggClientException e1) {
			LOG.info("Expected exception as it is an invalid phase: " + phase);
		}
	}

	 
}