package zingg.common.client.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestPathUtil {

    @Test
    public void testGetSparkLocationFromPath(){
        String input1 = "zingg/spark/model/blocks/-1111";
        String input2 = "zingg/spark/model/blocks/11_11";
        String exp1 = "zingg/spark/model/blocks/1111";
        String exp2 = "zingg/spark/model/blocks/11_11";
        assertEquals(exp1, PathUtil.getSparkLocationFromPath(input1));
        assertEquals(exp2, PathUtil.getSparkLocationFromPath(input2));
    }

    @Test
    public void testGetSnowTableFromPath(){
        String input1 = "zingg_snowflake_model_blocks_-1111";
        String input2 = "zingg/snowflake/model/blocks/-1111";
        String exp1 = "zingg_snowflake_model_blocks_1111";
        String exp2 = "zingg_snowflake_model_blocks_1111";
        assertEquals(exp1, PathUtil.getSnowTableFromPath(input1));
        assertEquals(exp2, PathUtil.getSnowTableFromPath(input2));
    }
    
}
