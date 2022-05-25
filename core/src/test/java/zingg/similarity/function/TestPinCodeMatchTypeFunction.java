package zingg.similarity.function;


import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPinCodeMatchTypeFunction {
	
	
	@Test
	public void testFirstPinNull() {
		PinCodeMatchTypeFunction PinMatch = new PinCodeMatchTypeFunction();
		assertEquals(1d, PinMatch.call(null, "34567"));
	}

	@Test
	public void testFirstPinEmpty() {
		PinCodeMatchTypeFunction PinMatch = new PinCodeMatchTypeFunction();
		assertEquals(1d, PinMatch.call("", "28390-6890"));
	}

	@Test
	public void testSecondPinNull() {
		PinCodeMatchTypeFunction PinMatch = new PinCodeMatchTypeFunction();
		assertEquals(1d, PinMatch.call("93949-1894", null));
	}

	@Test
	public void testSecondPinEmpty() {
		PinCodeMatchTypeFunction PinMatch = new PinCodeMatchTypeFunction();
		assertEquals(1d, PinMatch.call("31249", ""));
	}
	@Test
	public void testBothEmpty() {
		PinCodeMatchTypeFunction PinMatch = new PinCodeMatchTypeFunction();
		assertEquals(1d, PinMatch.call("", ""));
	}

	@Test
	public void testBothNull() {
		PinCodeMatchTypeFunction PinMatch = new PinCodeMatchTypeFunction();
		assertEquals(1d, PinMatch.call(null, null));
	}

	@Test
	public void testBothNotEmptysame1() {
		PinCodeMatchTypeFunction PinMatch = new PinCodeMatchTypeFunction();
		assertEquals(1d, PinMatch.call("45678", "45678"));
	}

	@Test
	public void testBothNotEmptydiff1() {
		PinCodeMatchTypeFunction PinMatch = new PinCodeMatchTypeFunction();
		assertEquals(0d, PinMatch.call("34234", "34334"));
	}

	@Test
	public void testBothNotEmptysame2() {
		PinCodeMatchTypeFunction PinMatch = new PinCodeMatchTypeFunction();
		assertEquals(1d, PinMatch.call("23412-9838", "23412-6934"));
	}

	@Test
	public void testBothNotEmptydiff2() {
		PinCodeMatchTypeFunction PinMatch = new PinCodeMatchTypeFunction();
		assertEquals(0d, PinMatch.call("34625-2153", "34325-2153"));
	}

	@Test
	public void testBothNotEmptysame12() {
		PinCodeMatchTypeFunction PinMatch = new PinCodeMatchTypeFunction();
		assertEquals(1d, PinMatch.call("79492", "79492-3943"));
	}

	@Test
	public void testBothNotEmptydiff12() {
		PinCodeMatchTypeFunction PinMatch = new PinCodeMatchTypeFunction();
		assertEquals(0d, PinMatch.call("87248", "87238-9024"));
	}

	@Test
	public void testBothNotEmptysame21() {
		PinCodeMatchTypeFunction PinMatch = new PinCodeMatchTypeFunction();
		assertEquals(1d, PinMatch.call("89827-9703", "89827"));
	}

	@Test
	public void testBothNotEmptydiff21() {
		PinCodeMatchTypeFunction PinMatch = new PinCodeMatchTypeFunction();
		assertEquals(0d, PinMatch.call("88082-9828", "81034"));
	}

}
