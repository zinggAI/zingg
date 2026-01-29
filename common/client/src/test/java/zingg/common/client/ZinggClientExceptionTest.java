package zingg.common.client;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ZinggClientExceptionTest {

    @Test
    void testMessageOnlyConstructor() {
        String msg = "simple error";

        ZinggClientException ex = new ZinggClientException(msg);

        assertEquals(msg, ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void testMessageAndCauseConstructor() {
        String msg = "wrapped error";
        Throwable cause = new Exception("root cause");

        ZinggClientException ex = new ZinggClientException(msg, cause);

        assertEquals(msg, ex.getMessage());
        assertNotNull(ex.getCause());
        assertSame(cause, ex.getCause());
        assertEquals("root cause", ex.getCause().getMessage());
    }

    @Test
    void testCauseOnlyConstructor() {
        Throwable cause = new Exception("original failure");

        ZinggClientException ex = new ZinggClientException(cause);

        assertEquals(cause.toString(), ex.getMessage());
        assertNotNull(ex.getCause());
        assertSame(cause, ex.getCause());
    }

    @Test
    void testExceptionChainingDepth() {
        Throwable root = new Exception("level 1");
        ZinggClientException level2 = new ZinggClientException("level 2", root);
        ZinggClientException level3 = new ZinggClientException("level 3", level2);

        assertEquals("level 3", level3.getMessage());
        assertSame(level2, level3.getCause());
        assertSame(root, level3.getCause().getCause());
        assertNull(level3.getCause().getCause().getCause());
    }

    @Test
    void shouldPreserveMultipleExceptionLayers() {

        ZinggClientException top =
                assertThrows(ZinggClientException.class, () -> {

                    try {
                        try {
                            throw new Exception("root exception");
                        } catch (Exception e) {
                            throw new ZinggClientException("level 1 exception", e);
                        }
                    } catch (ZinggClientException e) {
                        throw new ZinggClientException("level 2 exception", e);
                    }

                });

        assertEquals("level 2 exception", top.getMessage());

        Throwable level1 = top.getCause();
        assertNotNull(level1);
        assertTrue(level1 instanceof ZinggClientException);
        assertEquals("level 1 exception", level1.getMessage());

        Throwable root = level1.getCause();
        assertNotNull(root);
        assertEquals(Exception.class, root.getClass());
        assertEquals("root exception", root.getMessage());

        assertNull(root.getCause());
    }
}
