package zingg.common.core.similarity.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.Test;

public class TestDateSimilarityFunction {
	
	
	@Test
	public void testDateSimEqualDates() {
		DateSimilarityFunction dateSimFn = new DateSimilarityFunction();
		
		long curr = System.currentTimeMillis();
		
		Date d1 = new Date(curr);
		Date d2 = new Date(curr);
		
		assertEquals(1.0,dateSimFn.call(d1, d2));
		
	}
	
	@Test
	public void testDateSimCloseDates() {
		DateSimilarityFunction dateSimFn = new DateSimilarityFunction();
		
		long curr = System.currentTimeMillis();
		
		Date d1 = new Date(curr-2*24*60*60*1000);
		Date d2 = new Date(curr);
		
		assertTrue(dateSimFn.call(d1, d2)>0.99);
		
	}
	
	
	@Test
	public void testDateSimVeryFarDates() {
		DateSimilarityFunction dateSimFn = new DateSimilarityFunction();
		
		long curr = System.currentTimeMillis();
		
		Date d1 = new Date(curr);
		Date d2 = new Date(curr/200);
		
		assertTrue(dateSimFn.call(d1, d2)<0.01);
		
	}
	
	@Test
	public void testDateSimD1Null() {
		DateSimilarityFunction dateSimFn = new DateSimilarityFunction();
		
		long curr = System.currentTimeMillis();
		
		Date d1 = null;
		Date d2 = new Date(curr/2);
		
		assertEquals(1.0,dateSimFn.call(d1, d2));
		
	}

	@Test
	public void testDateSimD2Null() {
		DateSimilarityFunction dateSimFn = new DateSimilarityFunction();
		
		long curr = System.currentTimeMillis();
		
		Date d1 = new Date(curr/2);
		Date d2 = null;
		
		assertEquals(1.0,dateSimFn.call(d1, d2));
		
	}
	
	@Test
	public void testDateSimBothNull() {
		DateSimilarityFunction dateSimFn = new DateSimilarityFunction();
		
		Date d1 = null;
		Date d2 = null;
		
		assertEquals(1.0,dateSimFn.call(d1, d2));
		
	}
	
}
