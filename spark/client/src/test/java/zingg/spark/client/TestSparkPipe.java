package zingg.spark.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import zingg.spark.client.pipe.SparkPipe;

public class TestSparkPipe {

    @Test
    public void testMassageLocation(){
        String input = "zin:gg/spar,k/client/blo@cks/-1234";
        String expected = "zingg/spark/client/blocks/1234";
        assertEquals(expected, SparkPipe.massageLocation(input));
    }

    @Test
    public void testSetLocation(){
        String input = "zin:gg/spar,k/client/blo@cks/-1234";
        String exp = "zingg/spark/client/blocks/1234";
        Map<String, String> expProps = new HashMap<String, String>();
        expProps.put("location", exp);
        SparkPipe p = new SparkPipe();
        p.setLocation(input);
        Map<String, String> props = new HashMap<String, String>();
        props = p.getProps();
        assertEquals(props.get("location"), expProps.get("location"));
    }
    
}
