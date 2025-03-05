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
        assertEquals(2, value.call("india"));
    }
    
    @Test
    public void testFirst3CharsBox4() {
        First3CharsBox value = getInstance();
        assertEquals(3, value.call("izzze"));
    }

    @Test
    public void testFirst3CharsBox5() {
        First3CharsBox value = getInstance();
        assertEquals(4, value.call("noddy"));
    }
    
    @Test
    public void testFirst3CharsBox6() {
        First3CharsBox value = getInstance();
        assertEquals(5, value.call("sunday"));
    }

    @Test
    public void testFirst3CharsBox7() {
        First3CharsBox value = getInstance();
        assertEquals(6, value.call("uzzzz"));
    }

    @Test
    public void testFirst3CharsBox8() {
        First3CharsBox value = getInstance();
        assertEquals(6, value.call("xyzxyz"));
    }

    @Test
    public void testFirst3CharsBoxForShortWord() {
        First3CharsBox value = getInstance();
        assertEquals(0, value.call("ab"));
    }

    @Test
    public void testFirst3CharsBoxForNull() {
        First3CharsBox value = getInstance();
        assertEquals(0, value.call(null));
    }

    private First3CharsBox getInstance() {
        return new First3CharsBox();
    }


}
