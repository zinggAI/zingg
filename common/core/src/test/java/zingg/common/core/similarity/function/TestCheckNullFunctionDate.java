package zingg.common.core.similarity.function;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

import org.junit.jupiter.api.Test;
public class TestCheckNullFunctionDate {
	
	@Test
	public void testFirstNull() {
		assertEquals(0d, simFunc().call(null, new Date(2)));
	}

	@Test
	public void testSecondNull() {
		assertEquals(0d, simFunc().call(new Date(1), null));
	}

	@Test
	public void testBothNull() {
		assertEquals(0d, simFunc().call(null, null));
	}

	@Test
	public void testBothNotNull() {
		assertEquals(1d, simFunc().call(new Date(1), new Date(2)));
	}

	protected CheckNullFunction<Date> simFunc() {
		return new CheckNullFunction<Date>("CheckNullFunctionDate");
	}

}
