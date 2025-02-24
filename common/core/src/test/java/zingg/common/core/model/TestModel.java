package zingg.common.core.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

public class TestModel {

    @Test
    public void testGetGrid() {
        double[] result = Model.getGrid(1.0, 10.0, 2.0, false);
        double[] expected = {1.0, 3.0, 5.0, 7.0, 9.0};
        assertArrayEquals(expected, result, 0.0);
    }

    @Test
    public void testGetGridForMultiples() {
        double[] result = Model.getGrid(1.0, 10.0, 2.0, true);
        double[] expected = {1.0, 2.0, 4.0, 8.0};
        assertArrayEquals(expected, result, 0.0);
    }
    
}
