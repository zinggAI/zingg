package zingg.spark.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestRound {
	
	@Test
	public void testRound1() {
	    SparkRound value = getInstance();
		assertEquals(543535, value.call(543534.677));
	}

    @Test
    public void testRound2() {
        SparkRound value = getInstance();
        assertEquals(543534, value.call(543534.377));
    }
	
	
    private SparkRound getInstance() {
        return new SparkRound();
    }


}
