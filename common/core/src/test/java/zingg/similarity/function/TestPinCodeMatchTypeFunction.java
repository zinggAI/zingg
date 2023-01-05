package zingg.similarity.function;


import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPinCodeMatchTypeFunction {
	
	
	@Test
	public void testFirstPinNull() {
		PinCodeMatchTypeFunction pincodeMatchFn = new PinCodeMatchTypeFunction();
		assertEquals(1d, pincodeMatchFn.call(null, "34567"));
	}

	@Test
	public void testFirstPinEmpty() {
		PinCodeMatchTypeFunction pincodeMatchFn = new PinCodeMatchTypeFunction();
		assertEquals(1d, pincodeMatchFn.call("", "28390-6890"));
	}

	@Test
	public void testSecondPinNull() {
		PinCodeMatchTypeFunction pincodeMatchFn = new PinCodeMatchTypeFunction();
		assertEquals(1d, pincodeMatchFn.call("93949-1894", null));
	}

	@Test
	public void testSecondPinEmpty() {
		PinCodeMatchTypeFunction pincodeMatchFn = new PinCodeMatchTypeFunction();
		assertEquals(1d, pincodeMatchFn.call("31249", ""));
	}
	@Test
	public void testBothEmpty() {
		PinCodeMatchTypeFunction pincodeMatchFn = new PinCodeMatchTypeFunction();
		assertEquals(1d, pincodeMatchFn.call("", ""));
	}

	@Test
	public void testBothNull() {
		PinCodeMatchTypeFunction pincodeMatchFn = new PinCodeMatchTypeFunction();
		assertEquals(1d, pincodeMatchFn.call(null, null));
	}

	@Test
	public void testBothNotEmptyExact() {
		PinCodeMatchTypeFunction pincodeMatchFn = new PinCodeMatchTypeFunction();
		assertEquals(1d, pincodeMatchFn.call("45678", "45678"));
	}

	@Test
	public void testBothNotEmptyDifferent() {
		PinCodeMatchTypeFunction pincodeMatchFn = new PinCodeMatchTypeFunction();
		assertEquals(0d, pincodeMatchFn.call("34234", "34334"));
	}

	@Test
	public void testFirstPartMatch() {
		PinCodeMatchTypeFunction pincodeMatchFn = new PinCodeMatchTypeFunction();
		assertEquals(1d, pincodeMatchFn.call("23412-9838", "23412-6934"));
	}

	@Test
	public void testFirstPartDifferentSecondPartMatch() {
		PinCodeMatchTypeFunction pincodeMatchFn = new PinCodeMatchTypeFunction();
		assertEquals(0d, pincodeMatchFn.call("34625-2153", "34325-2153"));
	}

	@Test
	public void testOnlyFirstPartMatch() {
		PinCodeMatchTypeFunction pincodeMatchFn = new PinCodeMatchTypeFunction();
		assertEquals(1d, pincodeMatchFn.call("79492", "79492-3943"));
	}

	@Test
	public void testFirstPartDifferent() {
		PinCodeMatchTypeFunction pincodeMatchFn = new PinCodeMatchTypeFunction();
		assertEquals(0d, pincodeMatchFn.call("87248", "87238-9024"));
	}

	@Test
	public void testFirstPartmatch() {
		PinCodeMatchTypeFunction pincodeMatchFn = new PinCodeMatchTypeFunction();
		assertEquals(1d, pincodeMatchFn.call("89827-9703", "89827"));
	}
}
