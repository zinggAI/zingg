package zingg.common.core.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class TestBlockFunction {

    @Test
    public void knownJavaStringCollisionsProduceDifferentBlockingKeys() {
        assertEquals("Aa".hashCode(), "BB".hashCode());
        assertNotEquals(BlockFunction.hashBlockingPath("Aa"), BlockFunction.hashBlockingPath("BB"));
        assertEquals(BlockFunction.hashBlockingPath("Aa"), BlockFunction.hashBlockingPath("Aa"));
    }
}
