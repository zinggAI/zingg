package zingg.common.core.similarity.function;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

import org.junit.jupiter.api.Test;
public class TestCheckNullFunctionDate {
	
	
	@Test
	public void testFirstNull() {
		CheckNullFunctionDate isNull = new CheckNullFunctionDate();
		assertEquals(0d, isNull.call(null, new Date(2)));
	}


	@Test
	public void testSecondNull() {
		CheckNullFunctionDate isNull = new CheckNullFunctionDate();
		assertEquals(0d, isNull.call(new Date(1), null));
	}

	@Test
	public void testBothNull() {
		CheckNullFunctionDate isNull = new CheckNullFunctionDate();
		assertEquals(0d, isNull.call(null, null));
	}

	@Test
	public void testBothNotNull() {
		CheckNullFunctionDate isNull = new CheckNullFunctionDate();
		assertEquals(1d, isNull.call(new Date(1), new Date(2)));
	}

}
