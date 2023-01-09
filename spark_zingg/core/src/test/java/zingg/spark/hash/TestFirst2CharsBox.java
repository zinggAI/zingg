package zingg.spark.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestFirst2CharsBox {
	
	@Test
	public void testFirst2CharsBox1() {
	    SparkFirst2CharsBox value = getInstance();
		assertEquals(1, value.call("apple"));
	}

    @Test
    public void testFirst2CharsBox2() {
        SparkFirst2CharsBox value = getInstance();
        assertEquals(2, value.call("kite"));
    }
	
    @Test
    public void testFirst2CharsBox3() {
        SparkFirst2CharsBox value = getInstance();
        assertEquals(3, value.call("peacock"));
    }
  
    
    private SparkFirst2CharsBox getInstance() {
        return new SparkFirst2CharsBox();
    }


}
