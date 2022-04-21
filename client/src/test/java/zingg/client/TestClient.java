package zingg.client;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

public class TestClient {

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
			System.out.println("Expected exception as it is an invalid phase: " + phase);
		}
	}
}