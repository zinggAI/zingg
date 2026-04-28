package zingg.hash;

import org.junit.jupiter.api.Test;
import zingg.common.core.hash.IdentityDate;
import zingg.common.core.hash.IdentityInteger;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestIdentityDate {

    @Test
    public void testIdentityInteger() {
        IdentityDate value = getInstance();
        assertEquals(20020624, value.call(new Date("2002/06/24")));
    }

    @Test
    public void testNullValue() {
        IdentityDate value = getInstance();
        assertEquals(null, value.call(null));
    }

    private IdentityDate getInstance() {
        return new IdentityDate();
    }
}
