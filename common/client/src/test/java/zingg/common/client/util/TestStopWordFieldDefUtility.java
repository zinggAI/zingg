package zingg.common.client.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import zingg.common.client.Arguments;
import zingg.common.client.ArgumentsUtil;
import zingg.common.client.FieldDefinition;
import zingg.common.client.IArguments;
import zingg.common.client.TestFieldDefUtil;
import zingg.common.client.ZinggClientException;

public class TestStopWordFieldDefUtility {

    public static final Log LOG = LogFactory.getLog(TestFieldDefUtil.class);
	protected ArgumentsUtil<Arguments> argsUtil = new ArgumentsUtil<Arguments>(Arguments.class);

    StopWordFieldDefUtility stopWordFieldDefUtil = new StopWordFieldDefUtility();

    @Test
    public void testgetFieldDefinitionWithStopwords(){
        IArguments args;
            try {
                args = argsUtil.createArgumentsFromJSON(getClass().getResource("../../../../testArguments/configWithStopWords.json").getFile(), "test");

                List<? extends FieldDefinition> dontUseList = stopWordFieldDefUtil.getFieldDefinitionWithStopwords(args.getFieldDefinition());
                assertEquals(dontUseList.size(), 2);
                
            } catch (Exception | ZinggClientException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
				fail("Could not read config");
            }

    }
    
}
