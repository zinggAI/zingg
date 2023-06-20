package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import zingg.common.core.hash.IdentityLong;

public class TestIdentityLong {

    @Test
    public void testIdentityLong() {
        IdentityLong value = getInstance();
        assertEquals(12345L, value.call(12345L));
    }

    @Test
    public void testIdentityLong1() {
        IdentityLong value = getInstance();
        assertEquals(null, value.call(null));
    }

    private IdentityLong getInstance() {
        return new IdentityLong();
    }
    
}
