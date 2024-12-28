package zingg.common.core.similarity.function;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCheckBlankOrNullFunction {
	
	
	@Test
	public void testFirstWordNull() {
		CheckBlankOrNullFunction isNull = new CheckBlankOrNullFunction();
		assertEquals(0d, isNull.call(null, "second"));
	}

	@Test
	public void testFirstWordEmpty() {
		CheckBlankOrNullFunction isNull = new CheckBlankOrNullFunction();
		assertEquals(0d, isNull.call("", "second"));
	}

	@Test
	public void testSecondWordNull() {
		CheckBlankOrNullFunction isNull = new CheckBlankOrNullFunction();
		assertEquals(0d, isNull.call("f", null));
	}

	@Test
	public void testSecondWordEmpty() {
		CheckBlankOrNullFunction isNull = new CheckBlankOrNullFunction();
		assertEquals(0d, isNull.call("f", ""));
	}
	@Test
	public void testBothEmpty() {
		CheckBlankOrNullFunction isNull = new CheckBlankOrNullFunction();
		assertEquals(0d, isNull.call("", ""));
	}

	@Test
	public void testBothNull() {
		CheckBlankOrNullFunction isNull = new CheckBlankOrNullFunction();
		assertEquals(0d, isNull.call(null, null));
	}

	@Test
	public void testBothNotNullOrEmpty() {
		CheckBlankOrNullFunction isNull = new CheckBlankOrNullFunction();
		assertEquals(1d, isNull.call("not null", "not null"));
	}

}
