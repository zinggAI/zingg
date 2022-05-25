package zingg.similarity.function;


import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestEmailMatchTypeFunction {
	
	
	@Test
	public void testFirstEntryNull() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		assertEquals(1d, emailMatchFn.call(null, "xyz321@pqr.co"));
	}

	@Test
	public void testFirstEntryEmpty() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		assertEquals(1d, emailMatchFn.call("", "xyz321@pqr.co"));
	}

	@Test
	public void testSecondEntryNull() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		assertEquals(1d, emailMatchFn.call("xyz321@pqr.co", null));
	}

	@Test
	public void testSecondEntryEmpty() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		assertEquals(1d, emailMatchFn.call("xyz321@pqr.co", ""));
	}
	@Test
	public void testBothEmpty() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		assertEquals(1d, emailMatchFn.call("", ""));
	}

	@Test
	public void testBothNull() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		assertEquals(1d, emailMatchFn.call(null, null));
	}

	@Test
	public void testBothExact() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		assertEquals(1d, emailMatchFn.call("xyz321@pqr.co", "xyz321@pqr.co"));
	}

	@Test
	public void testbothDifferent() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		assertEquals(0d, emailMatchFn.call("xyz321@pqr.co", "pqr981@abc.in"));
	}

	@Test
	public void testFirstPartMatch() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		assertEquals(1d, emailMatchFn.call("pqr981@abc.in", "pqr981@xyz.com"));
	}

	@Test
	public void testFirstPartDifferentSecondPartMatch() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		assertEquals(0d, emailMatchFn.call("pqr981@xyz.com", "aqr981@xyz.com"));
	}

}
