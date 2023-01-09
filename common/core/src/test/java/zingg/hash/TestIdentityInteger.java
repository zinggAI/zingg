package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestIdentityInteger {
	
	@Test
	public void testIdentityInteger() {
	    IdentityInteger value = getInstance();
		assertEquals(100101, value.call(100101));
	}

    private IdentityInteger getInstance() {
        return new IdentityInteger();
    }


}
