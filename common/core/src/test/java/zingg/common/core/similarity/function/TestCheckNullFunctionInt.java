package zingg.common.core.similarity.function;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestCheckNullFunctionInt {
	
	
	@Test
	public void testFirstNull() {
		assertEquals(0d, simFunc().call(null, 2));
	}

	@Test
	public void testSecondNull() {
		assertEquals(0d, simFunc().call(1, null));
	}

	@Test
	public void testBothNull() {
		assertEquals(0d, simFunc().call(null, null));
	}

	@Test
	public void testBothNotNull() {
		assertEquals(1d, simFunc().call(1, 2));
	}

	protected CheckNullFunction<Integer> simFunc() {
		return new CheckNullFunction<Integer>("CheckNullFunctionInt");
	}

}
