package zingg.common.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import zingg.common.client.arguments.model.Arguments;
import zingg.common.client.arguments.model.IArguments;


public class TestFieldDefUtil {

	public static final Log LOG = LogFactory.getLog(TestFieldDefUtil.class);
	protected ArgumentsUtil<Arguments> argsUtil = new ArgumentsUtil<Arguments>(Arguments.class);
	
	protected FieldDefUtil fieldDefUtil = new FieldDefUtil();

	@Test
	public void testMatchTypeFilter() {
			IArguments args;
            try {
                args = argsUtil.createArgumentsFromJSON(getClass().getResource("../../../testArguments/configTestDontUse.json").getFile(), "test");
                List<? extends FieldDefinition> dontUseList = fieldDefUtil.getFieldDefinitionDontUse(args.getFieldDefinition()); 
                assertEquals(dontUseList.size(), 3);
                
                List<? extends FieldDefinition> matchList = fieldDefUtil.getFieldDefinitionToUse(args.getFieldDefinition());
                assertEquals(matchList.size(), 4);
                
            } catch (Exception | ZinggClientException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
				fail("Could not read config");
            }
		
	}
	
	
}
