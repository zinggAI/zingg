package zingg.spark.hash;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TestIsNullOrEmpty {
	
	@Test
	public void testIsNull() {
	    SparkIsNullOrEmpty value = getInstance();
		assertTrue(value.call(null));
	}
	
    @Test
    public void testIsEmpty() {
        SparkIsNullOrEmpty value = getInstance();
        assertTrue(value.call(""));
    }
    
    @Test
    public void testIsNeither() {
        SparkIsNullOrEmpty value = getInstance();
        assertFalse(value.call("unhappy"));
    }

    private SparkIsNullOrEmpty getInstance() {
        return new SparkIsNullOrEmpty();
    }


}
