package zingg.similarity.function;


import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestEmailMatchTypeFunction {
	
	
	@Test
	public void testFirstEntryNull() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		String first = null;
		String second = "xyz321@pqr.co";
		assertEquals(AffineGapSimilarityFunction(first,second.split("@",0)[0]), emailMatchFn.call(first, second));
	}

	@Test
	public void testFirstEntryEmpty() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		String first = "";
		String second = "xyz321@pqr.co";
		assertEquals(AffineGapSimilarityFunction(first.split("@",0)[0],second.split("@",0)[0]), emailMatchFn.call(first, second));
	}

	@Test
	public void testSecondEntryNull() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		String first = "xyz321@pqr.co";
		String second = null;
		assertEquals(AffineGapSimilarityFunction(first.split("@",0)[0],second), emailMatchFn.call(first,second));
	}

	@Test
	public void testSecondEntryEmpty() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		String first = "xyz321@pqr.co";
		String second = "";
		assertEquals(AffineGapSimilarityFunction(first.split("@",0)[0],second.split("@",0)[0]), emailMatchFn.call(first,second));
	}
	@Test
	public void testBothEmpty() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		String first = "";
		String second = "";
		assertEquals(AffineGapSimilarityFunction(first.split("@",0)[0],second.split("@",0)[0]), emailMatchFn.call(first,second));
	}

	@Test
	public void testBothNull() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		String first = null;
		String second = null;
		assertEquals(1d, emailMatchFn.call(first,second));
	}

	@Test
	public void testBothExact() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		String first = "xyz321@pqr.co";
		String second = "xyz321@pqr.co";
		assertEquals(1d, emailMatchFn.call(first,second));
	}

	@Test
	public void testbothDifferent() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		String first = "xyz321@pqr.co";
		String second = "pqr981@abc.in";
		assertEquals(AffineGapSimilarityFunction(first.split("@",0)[0],second.split("@",0)[0]), emailMatchFn.call(first,second));
	}

	@Test
	public void testFirstPartMatch() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		String first = "pqr981@abc.in";
		String second = "pqr981@xyz.com";
		assertEquals(1d, emailMatchFn.call(first,second));
	}

	@Test
	public void testFirstPartDifferentSecondPartMatch() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		String first = "pqr981@xyz.com";
		String second = "aqr981@xyz.com";
		assertEquals(AffineGapSimilarityFunction(first.split("@",0)[0],second.split("@",0)[0]), emailMatchFn.call(first,second));
	}

}
