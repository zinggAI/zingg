package zingg.common.core.similarity.function;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestCheckNullFunctionLong {
	
	
	@Test
	public void testFirstNull() {
		assertEquals(0d, simFunc().call(null, 2l));
	}

	@Test
	public void testSecondNull() {
		assertEquals(0d, simFunc().call(1l, null));
	}

	@Test
	public void testBothNull() {
		assertEquals(0d, simFunc().call(null, null));
	}

	@Test
	public void testBothNotNull() {
		assertEquals(1d, simFunc().call(1l, 2l));
	}

	protected CheckNullFunction<Long> simFunc() {
		return new CheckNullFunction<Long>("CheckNullFunctionLong");
	}

}
