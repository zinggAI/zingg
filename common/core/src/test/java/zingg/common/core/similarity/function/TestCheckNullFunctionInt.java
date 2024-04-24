package zingg.common.core.similarity.function;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestCheckNullFunctionInt {
	
	
	@Test
	public void testFirstNull() {
		CheckNullFunction<Integer> isNull = new CheckNullFunction<Integer>("CheckNullFunctionInt");
		assertEquals(0d, isNull.call(null, 2));
	}


	@Test
	public void testSecondNull() {
		CheckNullFunction<Integer> isNull = new CheckNullFunction<Integer>("CheckNullFunctionInt");
		assertEquals(0d, isNull.call(1, null));
	}

	@Test
	public void testBothNull() {
		CheckNullFunction<Integer> isNull = new CheckNullFunction<Integer>("CheckNullFunctionInt");
		assertEquals(0d, isNull.call(null, null));
	}

	@Test
	public void testBothNotNull() {
		CheckNullFunction<Integer> isNull = new CheckNullFunction<Integer>("CheckNullFunctionInt");
		assertEquals(1d, isNull.call(1, 2));
	}

}
