package zingg.common.client.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;


public class TestStringRedactor {

    
    @ParameterizedTest
    @MethodSource("mapProvider")
    public void testRedactions(Map<String,String> map, String expectedValue){
        System.out.println("map is " + map);
        System.out.println("expected val is " + expectedValue);
        assertEquals(new StringRedactor().redact(map), expectedValue);
    }

    static class Args {
        public HashMap<String, String> props;
        public String expectedVal;
    }

    static Stream<Arguments> mapProvider() {
        Map<String, String> args1 = new HashMap<String, String>();
        args1.put("non redacted", "value");
        String args1ExpectedVal = "{non redacted=value}";

        Map<String, String> args2 = new HashMap<String, String>();
        args2.put("password", "value");
        String args1ExpectedVal2 = "{password=********(redacted)}";

        return Stream.of(
            arguments(args1, args1ExpectedVal),
            arguments(args2, args1ExpectedVal2)
        );
}
    
}
