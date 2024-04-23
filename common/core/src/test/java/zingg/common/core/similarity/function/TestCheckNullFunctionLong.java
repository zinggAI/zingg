package zingg.common.core.similarity.function;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestCheckNullFunctionLong {
	
	
	@Test
	public void testFirstNull() {
		CheckNullFunctionLong isNull = new CheckNullFunctionLong();
		assertEquals(0d, isNull.call(null, 2l));
	}


	@Test
	public void testSecondNull() {
		CheckNullFunctionLong isNull = new CheckNullFunctionLong();
		assertEquals(0d, isNull.call(1l, null));
	}

	@Test
	public void testBothNull() {
		CheckNullFunctionLong isNull = new CheckNullFunctionLong();
		assertEquals(0d, isNull.call(null, null));
	}

	@Test
	public void testBothNotNull() {
		CheckNullFunctionLong isNull = new CheckNullFunctionLong();
		assertEquals(1d, isNull.call(1l, 2l));
	}

}
