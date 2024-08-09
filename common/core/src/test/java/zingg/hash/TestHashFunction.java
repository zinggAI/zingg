package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import zingg.common.client.ZFrame;
import zingg.common.core.hash.HashFunction;


public class TestHashFunction {
    @Test
    public void testGetName() {
        HashFunction<String, Integer, Boolean, Long> hashFunction = new HashFunction<String, Integer, Boolean, Long>("initialName") {
            @Override
            public ZFrame<String, Integer, Boolean> apply(ZFrame<String, Integer, Boolean> ds, String column, String newColumn) {
                return null;
            }

            @Override
            public Object getAs(Integer integer, String column) {
                return null;
            }

            @Override
            public Object getAs(String s, Integer integer, String column) {
                return null;
            }

            @Override
            public Object apply(Integer integer, String column) {
                return null;
            }

            @Override
            public Object apply(String s, Integer integer, String column) {
                return null;
            }
        };

        String expectedName = "hashFunction";
        hashFunction.setName(expectedName);
        assertEquals(expectedName, hashFunction.getName());
    }
    @Test
    public void testGetReturnType() {
        HashFunction<String, Integer, Boolean, Long> hashFunction = new HashFunction<String, Integer, Boolean, Long>("Name", 999L, 888L) {
            @Override
            public ZFrame<String, Integer, Boolean> apply(ZFrame<String, Integer, Boolean> ds, String column, String newColumn) {
                return null;
            }

            @Override
            public Object getAs(Integer integer, String column) {
                return null;
            }

            @Override
            public Object getAs(String s, Integer integer, String column) {
                return null;
            }

            @Override
            public Object apply(Integer integer, String column) {
                return null;
            }

            @Override
            public Object apply(String s, Integer integer, String column) {
                return null;
            }
        };

        long returnType = 9999L;
        hashFunction.setReturnType(returnType);
        assertEquals(returnType, hashFunction.getReturnType());

        long dataType = 888L;
        hashFunction.setDataType(dataType);
        assertEquals(dataType, hashFunction.getDataType());
    }

    @Test
    public void testIsUdf() {
        HashFunction<String, Integer, Boolean, Long> hashFunction = new HashFunction<String, Integer, Boolean, Long>("Name", 999L, 888L, true) {
            @Override
            public ZFrame<String, Integer, Boolean> apply(ZFrame<String, Integer, Boolean> ds, String column, String newColumn) {
                return null;
            }

            @Override
            public Object getAs(Integer integer, String column) {
                return null;
            }

            @Override
            public Object getAs(String s, Integer integer, String column) {
                return null;
            }

            @Override
            public Object apply(Integer integer, String column) {
                return null;
            }

            @Override
            public Object apply(String s, Integer integer, String column) {
                return null;
            }
        };

        Boolean isUdf = false;
        hashFunction.setUdf(isUdf);
        assertEquals(false, hashFunction.isUdf());
    }

    @Test
    public void testGetAs() {
        HashFunction<String, Integer, Boolean, Long> hashFunction = new HashFunction<String, Integer, Boolean, Long>() {
            @Override
            public ZFrame<String, Integer, Boolean> apply(ZFrame<String, Integer, Boolean> ds, String column, String newColumn) {
                return null;
            }

            @Override
            public Object getAs(Integer integer, String column) {
                return null;
            }

            @Override
            public Object getAs(String s, Integer integer, String column) {
                return null;
            }

            @Override
            public Object apply(Integer integer, String column) {
                return null;
            }

            @Override
            public Object apply(String s, Integer integer, String column) {
                return null;
            }
        };
        Integer value = 10;
        String column = "inputColumn";
        assertEquals(null, hashFunction.getAs(value, column));
    }
    
}
