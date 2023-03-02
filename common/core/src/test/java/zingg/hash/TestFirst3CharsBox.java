package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import zingg.common.core.hash.First3CharsBox;

public class TestFirst3CharsBox {
	
	@Test
	public void testFirst3CharsBox1() {
	    First3CharsBox value = getInstance();
		assertEquals(1, value.call("elephant"));
	}

    @Test
    public void testFirst3CharsBox2() {
        First3CharsBox value = getInstance();
        assertEquals(2, value.call("father"));
    }
	
    @Test
    public void testFirst3CharsBox3() {
        First3CharsBox value = getInstance();
        assertEquals(2, value.call("India"));
    }
  
    
    private First3CharsBox getInstance() {
        return new First3CharsBox();
    }


}
