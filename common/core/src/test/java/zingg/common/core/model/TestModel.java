package zingg.common.core.model;

import zingg.common.client.ZFrame;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

import zingg.common.core.model.Model;

import java.io.IOException;

public class TestModel {
    @Test
    public void testGetGrid() {
        Model<Object, Object, Object, Object, Object> model = getInstance();
        double[] result = model.getGrid(1.0, 10.0, 2.0, false);
        double[] expected = {1.0, 3.0, 5.0, 7.0, 9.0};
        assertArrayEquals(expected, result, 0.0);
    }

    @Test
    public void testGetGridForMultiples() {
        Model<Object, Object, Object, Object, Object> model = getInstance();
        double[] result = model.getGrid(1.0, 10.0, 2.0, true);
        double[] expected = {1.0, 2.0, 4.0, 8.0};
        assertArrayEquals(expected, result, 0.0);
    }

    private Model<Object, Object, Object, Object, Object> getInstance() {
        Model<Object, Object, Object, Object, Object> model = new Model<Object, Object, Object, Object, Object>() {
            @Override
            public void register(Object spark) {
            }

            @Override
            public void fit(ZFrame<Object, Object, Object> pos, ZFrame<Object, Object, Object> neg) {
            }

            @Override
            public void load(String path) {
            }

            @Override
            public ZFrame<Object, Object, Object> predict(ZFrame<Object, Object, Object> data) {
                return null;
            }

            @Override
            public ZFrame<Object, Object, Object> predict(ZFrame<Object, Object, Object> data, boolean isDrop) {
                return null;
            }

            @Override
            public void save(String path) throws IOException {
            }

            @Override
            public ZFrame<Object, Object, Object> transform(ZFrame<Object, Object, Object> input) {
                return null;
            }
        };
          return model;
    }

}
