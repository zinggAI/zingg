package zingg.common.client.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import zingg.common.client.Arguments;
import zingg.common.client.ArgumentsUtil;
import zingg.common.client.FieldDefinition;
import zingg.common.client.IArguments;
import zingg.common.client.MatchType;
import zingg.common.client.ZinggClientException;

public class TestStopWordFieldDefUtility {

    private static final Log LOG = LogFactory.getLog(TestStopWordFieldDefUtility.class);
	protected ArgumentsUtil<Arguments> argsUtil = new ArgumentsUtil<Arguments>(Arguments.class);

    StopWordFieldDefUtility stopWordFieldDefUtil = new StopWordFieldDefUtility();

    @Test
    public void testGetFieldDefinitionWithStopwords(){
        IArguments args;
            try {
                args = argsUtil.createArgumentsFromJSON(getClass().getResource("../../../../testArguments/configWithStopWords.json").getFile(), "test");

                List<? extends FieldDefinition> dontUseList = stopWordFieldDefUtil.getFieldDefinitionWithStopwords(args.getFieldDefinition());
                assertEquals(dontUseList.size(), 2);
                
            } catch (Exception | ZinggClientException e) {
                e.printStackTrace();
				fail("Could not read config");
            }

    }

    @Test
	public void testGetFieldDefinitionNamesWithStopwords() throws ZinggClientException{
		FieldDefinition def1 = new FieldDefinition();
		def1.setFieldName("field1");
		def1.setDataType("string");
		def1.setMatchTypeInternal(MatchType.FUZZY);
		def1.setFields("field1");

		FieldDefinition def2 = new FieldDefinition();
		def2.setFieldName("field2");
		def2.setDataType("string");
		def2.setMatchTypeInternal(MatchType.EXACT);
		def2.setStopWords("stopWordsFileName2");
		def2.setFields("field2");

		FieldDefinition def3 = new FieldDefinition();
		def3.setFieldName("field3");
		def3.setDataType("string");
		def3.setMatchTypeInternal(MatchType.FUZZY);
		def3.setStopWords("stopWordsFileName3");
		def3.setFields("field3");

		List<FieldDefinition> fieldDef = new ArrayList<FieldDefinition>();
		fieldDef.add(def1);
		fieldDef.add(def2);
		fieldDef.add(def3);
		IArguments args = null; 
		try {
			args = new Arguments();
			args.setFieldDefinition(fieldDef);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String result = stopWordFieldDefUtil.getFieldDefinitionNamesWithStopwords(args);
		assertEquals("field2, field3", result);

	}
    
}
