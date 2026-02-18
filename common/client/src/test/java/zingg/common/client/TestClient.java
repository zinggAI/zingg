package zingg.common.client;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import zingg.common.client.options.ZinggOptions;

public class TestClient {
	public static final Log LOG = LogFactory.getLog(TestClient.class);

	@Test
	public void testValidPhase() throws ZinggClientException {
		String phase = "train";
		ZinggOptions.verifyPhase(phase);
	}

	@Test
	public void testInvalidPhase() {
		String phase = "tain";

		assertThrows(ZinggClientException.class, () -> {
			ZinggOptions.verifyPhase(phase);
		});
	}


}