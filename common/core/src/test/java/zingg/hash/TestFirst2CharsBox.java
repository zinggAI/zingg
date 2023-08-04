package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import zingg.common.core.hash.First2CharsBox;

public class TestFirst2CharsBox {
	
	@Test
	public void testFirst2CharsBox1() {
	    First2CharsBox value = getInstance();
		assertEquals(1, value.call("apple"));
	}

    @Test
    public void testFirst2CharsBox2() {
        First2CharsBox value = getInstance();
        assertEquals(2, value.call("kite"));
    }
	
    @Test
    public void testFirst2CharsBox3() {
        First2CharsBox value = getInstance();
        assertEquals(3, value.call("peacock"));
    }
    @Test
    public void testFirst2CharsBox4() {
        First2CharsBox value = getInstance();
        assertEquals(0, value.call("a"));
    }
  
    
    private First2CharsBox getInstance() {
        return new First2CharsBox();
    }


}
