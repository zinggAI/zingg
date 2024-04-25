package zingg.common.core.similarity.function;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

import org.junit.jupiter.api.Test;

public class TestDateSimilarityFunctionExact {
	
	
	@Test
	public void testFirstNull() {
		assertEquals(1d, simFunc().call(null, new Date(2)));
	}


	@Test
	public void testSecondNull() {
		assertEquals(1d, simFunc().call(new Date(1), null));
	}

	@Test
	public void testBothNull() {
		assertEquals(1d, simFunc().call(null, null));
	}

	@Test
	public void testNotEqual() {
		assertEquals(0d, simFunc().call(new Date(101), new Date(102)));
	}

	@Test
	public void testEqual() {
		assertEquals(1d, simFunc().call(new Date(101), new Date(101)));
	}

	protected SimilarityFunctionExact<Date> simFunc() {
		return new SimilarityFunctionExact<Date>("DateSimilarityFunctionExact");
	}
	
}
