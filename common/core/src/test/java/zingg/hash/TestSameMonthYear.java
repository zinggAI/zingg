package zingg.hash;

import java.util.Date;
import org.junit.jupiter.api.Test;
import zingg.common.core.hash.SameMonthYear;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSameMonthYear {
    @Test
    public void testSameMonthYear() {
        SameMonthYear value = getInstance();
        assertEquals(200206, value.call(new Date("2002/06/24")));
    }

    @Test
    public void testSameMonthYear2() {
        SameMonthYear value = getInstance();
        assertEquals(value.call(new Date("2002/06/11")),
                    value.call(new Date("2002/06/24")));
    }

    @Test
    public void testNullValue() {
        SameMonthYear value = getInstance();
        assertEquals(null, value.call(null));
    }

    private SameMonthYear getInstance() {
        return new SameMonthYear();
    }


}
