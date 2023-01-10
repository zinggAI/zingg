package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TestIsNullOrEmpty {
	
	@Test
	public void testIsNull() {
	    IsNullOrEmpty value = getInstance();
		assertTrue(value.call(null));
	}
	
    @Test
    public void testIsEmpty() {
        IsNullOrEmpty value = getInstance();
        assertTrue(value.call(""));
    }
    
    @Test
    public void testIsNeither() {
        IsNullOrEmpty value = getInstance();
        assertFalse(value.call("unhappy"));
    }

    private IsNullOrEmpty getInstance() {
        return new IsNullOrEmpty();
    }


}
