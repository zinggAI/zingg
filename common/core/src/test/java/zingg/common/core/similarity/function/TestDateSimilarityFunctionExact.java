package zingg.common.core.similarity.function;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

import org.junit.jupiter.api.Test;

public class TestDateSimilarityFunctionExact {
	
	
	@Test
	public void testFirstNull() {
		SimilarityFunctionExact<Date> exact = new SimilarityFunctionExact<Date>("DateSimilarityFunctionExact");
		assertEquals(1d, exact.call(null, new Date(2)));
	}


	@Test
	public void testSecondNull() {
		SimilarityFunctionExact<Date> exact = new SimilarityFunctionExact<Date>("DateSimilarityFunctionExact");
		assertEquals(1d, exact.call(new Date(1), null));
	}

	@Test
	public void testBothNull() {
		SimilarityFunctionExact<Date> exact = new SimilarityFunctionExact<Date>("DateSimilarityFunctionExact");
		assertEquals(1d, exact.call(null, null));
	}

	@Test
	public void testNotEqual() {
		SimilarityFunctionExact<Date> exact = new SimilarityFunctionExact<Date>("DateSimilarityFunctionExact");
		assertEquals(0d, exact.call(new Date(101), new Date(102)));
	}

	@Test
	public void testEqual() {
		SimilarityFunctionExact<Date> exact = new SimilarityFunctionExact<Date>("DateSimilarityFunctionExact");
		assertEquals(1d, exact.call(new Date(101), new Date(101)));
	}
	
}
