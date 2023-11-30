package zingg.common.client.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.jupiter.api.Named.named;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class TestStringRedactor {
 
    @ParameterizedTest(name="{0}")
    @MethodSource("mapProvider")
    public void testRedactions(String testName, Map<String,String> map, String[] expectedValue, String[] unexpectedValue){
        String redacted = new StringRedactor().redact(map);
        System.out.println("map is " + map + " and redacted is " + redacted);
        for (String expected: expectedValue) {
            assertTrue(redacted.contains(expected), "did not find " + expected);
        }
        for (String unexpected: unexpectedValue) {
            assertTrue(!redacted.contains(unexpected), "unexpected " + unexpected);
        }
    }

    static class Args {
        public HashMap<String, String> props;
        public String expectedVal;
    }

    static Stream<Arguments> mapProvider() {
        Map<String, String> args1 = new HashMap<String, String>();
        args1.put("non redacted", "value");
        String[] expectedVal1 = new String[] {"non redacted=value"};
        String[] unexpectedVal1 = new String[] {"non redacted=***"};

        Map<String, String> args2 = new HashMap<String, String>();
        args2.put("password", "value");
        String[] expectedVal2 = new String[]{"password=********(redacted)"};
        String[] unexpectedVal2 = new String[] {"password=value"};

        Map<String, String> args3 = null;
        String[] expectedVal3 = new String[]{"{}"};
        String[] unexpectedVal3 = new String[] {"password=value"};

        Map<String, String> args4 = new HashMap<String, String>();
        args4.put("password", "value");
        args4.put("xpassword", "new value");
        String[] expectedVal4 =  new String[] {"password=********(redacted)", "xpassword=new value"};
        String[] unexpectedVal4 = new String[] {"password=value", "xpassword=***"};

        Map<String, String> args5 = new HashMap<String, String>();
        args5.put("token", "keyless");
        args5.put("secret", "value");
        String[] expectedVal5 = new String[] {"token=********(redacted)", "secret=********(redacted)"};
        String[] unexpectedVal5 = new String[] {"key=keyless", "password=value"};

        Map<String, String> args6 = new HashMap<String, String>();
        args6.put("accesskey", "keyless");
        String[] expectedVal6 = new String[] {"accesskey=********(redacted)"};
        String[] unexpectedVal6 = new String[] {"accesskey=keyless", "password=value"};

        Map<String, String> args7 = new HashMap<String, String>();
        args7.put("accessKey", "keyless");
        String[] expectedVal7 = new String[] {"accessKey=********(redacted)"};
        String[] unexpectedVal7 = new String[] {"accessKey=keyless", "password=value"};

        return Stream.of(
            arguments("noRedaction",args1, expectedVal1, unexpectedVal1),
            arguments("singlePasswordRedaction", args2, expectedVal2, unexpectedVal2),
            arguments("nullMap",args3, expectedVal3, unexpectedVal3),
            arguments("passwordAndXPasswd", args4, expectedVal4, unexpectedVal4),
            arguments("tokenAndSecret",args5, expectedVal5, unexpectedVal5),
            arguments("accesskey", args6, expectedVal6, unexpectedVal6),
            arguments("accessKeyCaps", args7, expectedVal7, unexpectedVal7)
        );
}
    
}
