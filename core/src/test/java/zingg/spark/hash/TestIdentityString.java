package zingg.spark.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestIdentityString {
	
	@Test
	public void testIdentityString() {
	    SparkIdentityString value = getInstance();
		assertEquals("unhappy", value.call(" UnHappy "));
	}

    private SparkIdentityString getInstance() {
        return new SparkIdentityString();
    }


}
