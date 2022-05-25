package zingg.similarity.function;


import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestEmailMatchTypeFunction {
	
	
	@Test
	public void testFirstEntryNull() {
		AffineGapSimilarityFunction emailMatchFn = new AffineGapSimilarityFunction();
		String first = null;
		String second = "xyz321@pqr.co";
		//first = first.split("@",0)[0];
		second = second.split("@",0)[0];
		assertEquals(1d, emailMatchFn.call(first, second));
	}

	@Test
	public void testFirstEntryEmpty() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		String first = "";
		String second = "xyz321@pqr.co";
		first = first.split("@",0)[0];
		second = second.split("@",0)[0];
		assertEquals(1d, emailMatchFn.call(first, second));
	}

	@Test
	public void testSecondEntryNull() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		String first = "xyz321@pqr.co";
		String second = null;
		first = first.split("@",0)[0];
		//second = second.split("@",0)[0];
		assertEquals(1d, emailMatchFn.call(first,second));
	}

	@Test
	public void testSecondEntryEmpty() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		String first = "xyz321@pqr.co";
		String second = "";
		first = first.split("@",0)[0];
		second = second.split("@",0)[0];
		assertEquals(1d, emailMatchFn.call(first,second));
	}
	@Test
	public void testBothEmpty() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		String first = "";
		String second = "";
		first = first.split("@",0)[0];
		second = second.split("@",0)[0];
		assertEquals(1d, emailMatchFn.call(first,second));
	}

	@Test
	public void testBothNull() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		String first = null;
		String second = null;
		//first = first.split("@",0)[0];
		//second = second.split("@",0)[0];
		assertEquals(1d, emailMatchFn.call(first,second));
	}

	@Test
	public void testBothExact() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		String first = "xyz321@pqr.co";
		String second = "xyz321@pqr.co";
		first = first.split("@",0)[0];
		second = second.split("@",0)[0];
		assertEquals(1d, emailMatchFn.call(first,second));
	}

	@Test
	public void testbothDifferent() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		String first = "xyz321@pqr.co";
		String second = "pqr981@abc.in";
		first = first.split("@",0)[0];
		second = second.split("@",0)[0];
		assertEquals(1d, emailMatchFn.call(first,second));
	}

	@Test
	public void testFirstPartMatch() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		String first = "pqr981@abc.in";
		String second = "pqr981@xyz.com";
		first = first.split("@",0)[0];
		second = second.split("@",0)[0];
		assertEquals(1d, emailMatchFn.call(first,second));
	}

	@Test
	public void testFirstPartDifferentSecondPartMatch() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		String first = "pqr981@xyz.com";
		String second = "aqr981@xyz.com";
		first = first.split("@",0)[0];
		second = second.split("@",0)[0];
		assertEquals(1d, emailMatchFn.call(first,second));
	}

}
