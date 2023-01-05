package zingg.spark.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestLastChars {
	
	@Test
	public void testLastChars() {
	    SparkLastChars value = getInstance(5);
		assertEquals("happy", value.call("unhappy"));
	}

    private SparkLastChars getInstance(int endIndex) {
        return new SparkLastChars(endIndex);
    }


}
