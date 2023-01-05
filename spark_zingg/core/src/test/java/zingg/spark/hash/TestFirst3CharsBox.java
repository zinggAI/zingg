package zingg.spark.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestFirst3CharsBox {
	
	@Test
	public void testFirst3CharsBox1() {
	    SparkFirst3CharsBox value = getInstance();
		assertEquals(1, value.call("elephant"));
	}

    @Test
    public void testFirst3CharsBox2() {
        SparkFirst3CharsBox value = getInstance();
        assertEquals(2, value.call("father"));
    }
	
    @Test
    public void testFirst3CharsBox3() {
        SparkFirst3CharsBox value = getInstance();
        assertEquals(2, value.call("India"));
    }
  
    
    private SparkFirst3CharsBox getInstance() {
        return new SparkFirst3CharsBox();
    }


}
