package zingg.common.client;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import zingg.common.client.options.ZinggOptions;

public class TestClient {

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