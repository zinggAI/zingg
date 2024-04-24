package zingg.common.core.similarity.function;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestCheckNullFunctionLong {
	
	
	@Test
	public void testFirstNull() {
		CheckNullFunction<Long> isNull = new CheckNullFunction<Long>("CheckNullFunctionLong");
		assertEquals(0d, isNull.call(null, 2l));
	}


	@Test
	public void testSecondNull() {
		CheckNullFunction<Long> isNull = new CheckNullFunction<Long>("CheckNullFunctionLong");
		assertEquals(0d, isNull.call(1l, null));
	}

	@Test
	public void testBothNull() {
		CheckNullFunction<Long> isNull = new CheckNullFunction<Long>("CheckNullFunctionLong");
		assertEquals(0d, isNull.call(null, null));
	}

	@Test
	public void testBothNotNull() {
		CheckNullFunction<Long> isNull = new CheckNullFunction<Long>("CheckNullFunctionLong");
		assertEquals(1d, isNull.call(1l, 2l));
	}

}
