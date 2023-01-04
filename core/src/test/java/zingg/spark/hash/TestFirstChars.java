package zingg.spark.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestFirstChars {
	
	@Test
	public void testFirstChars1() {
	    SparkFirstChars value = getInstance(2);
		assertEquals("un", value.call("unhappy"));
	}

    @Test
    public void testFirstChars2() {
        SparkFirstChars value = getInstance(12);
        assertEquals("unhappy", value.call("unhappy"));
    }
	
    private SparkFirstChars getInstance(int endIndex) {
        return new SparkFirstChars(endIndex);
    }


}
