package zingg.spark.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestLastWord {
	
	@Test
	public void testLastWord1() {
	    SparkLastWord value = getInstance();
		assertEquals("gupta", value.call("vikas gupta"));
	}

    @Test
    public void testLastWord2() {
        SparkLastWord value = getInstance();
        assertEquals("gupta", value.call("gupta"));
    }
	
	
    private SparkLastWord getInstance() {
        return new SparkLastWord();
    }


}
