package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import zingg.common.core.hash.Round;

public class TestRound {
	
	@Test
	public void testRound1() {
	    Round value = getInstance();
		assertEquals(543535, value.call(543534.677));
	}

    @Test
    public void testRound2() {
        Round value = getInstance();
        assertEquals(543534, value.call(543534.377));
    }

    @Test
    public void testRound3() {
        Round value = getInstance();
        assertEquals(null, value.call(null));
    }
	
	
    private Round getInstance() {
        return new Round();
    }


}
