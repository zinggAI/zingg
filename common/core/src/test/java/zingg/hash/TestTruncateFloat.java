package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import zingg.common.core.hash.TruncateFloat;

public class TestTruncateFloat {
    @Test
    public void testTruncateFloatTo1Place() {
        TruncateFloat value = getInstance(1);
        assertEquals(435.3f,value.call(435.391f));
    }
    @Test
    public void testTruncateFloatTo2Place() {
        TruncateFloat value = getInstance(2);
        assertEquals(435.39f, value.call(435.391f));
    }
    @Test
    public void testTruncateFloatTo3PlaceWhenNumHas2DecimalPlaces() {
        TruncateFloat value = getInstance(3);
        assertEquals(35.720f, value.call(35.72f));
    }
    @Test
    public void testTruncateFloatTo3PlaceWhenNumHasNoDecimalPlaces() {
        TruncateFloat value = getInstance(3);
        assertEquals(32.000f, value.call(32f));
    }
    @Test
	public void testTruncateFloatTo3PlaceForNumber0() {
	    TruncateFloat value =  getInstance(3);
		assertEquals(0f, value.call(0f));
	}

	@Test
	public void testTruncateFloatTo3PlaceForNull() {
	    TruncateFloat value =  getInstance(3);
		assertEquals(null, value.call(null));
	}
	

    private TruncateFloat getInstance(int num) {
        return new TruncateFloat(num);
    }
}
