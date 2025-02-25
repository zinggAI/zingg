package zingg.common.client.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import zingg.common.client.Arguments;
import zingg.common.client.FieldDefinition;
import zingg.common.client.IArguments;
import zingg.common.client.MatchType;
import zingg.common.client.MatchTypes;
import zingg.common.client.ZinggClientException;

public class TestStopWordUtility {

    @Test
    public void testGetFieldDefinitionWithStopwords(){
        try {
                FieldDefinition def1 = new FieldDefinition();
		        def1.setFieldName("field1");
		        def1.setDataType("string");
		        def1.setMatchTypeInternal(MatchTypes.FUZZY);
		        def1.setFields("field1");

                FieldDefinition def2 = new FieldDefinition();
                def2.setFieldName("field2");
                def2.setDataType("string");
                def2.setMatchTypeInternal(MatchTypes.EXACT);
                def2.setStopWords("stopWordsFileName2");
                def2.setFields("field2");

                FieldDefinition def3 = new FieldDefinition();
                def3.setFieldName("field3");
                def3.setDataType("string");
                def3.setMatchTypeInternal(MatchTypes.FUZZY);
				def3.setStopWords(null);
                def3.setFields("field3");

                List<FieldDefinition> fieldDef = new ArrayList<FieldDefinition>();
                fieldDef.add(def1);
                fieldDef.add(def2);
                fieldDef.add(def3);

                List<? extends FieldDefinition> stopWordList = new StopWordUtility().getFieldDefinitionWithStopwords(fieldDef);
                assertEquals(1,stopWordList.size());
				assertEquals("field2", stopWordList.get(0).getName());
                
            } catch (Exception e) {
                e.printStackTrace();

            }

    }

    @Test
	public void testGetFieldDefinitionNamesWithStopwords() throws ZinggClientException{
		FieldDefinition def1 = new FieldDefinition();
		def1.setFieldName("field1");
		def1.setDataType("string");
		def1.setMatchTypeInternal(MatchTypes.FUZZY);
		def1.setFields("field1");

		FieldDefinition def2 = new FieldDefinition();
		def2.setFieldName("field2");
		def2.setDataType("string");
		def2.setMatchTypeInternal(MatchTypes.EXACT);
		def2.setStopWords("stopWordsFileName2");
		def2.setFields("field2");

		FieldDefinition def3 = new FieldDefinition();
		def3.setFieldName("field3");
		def3.setDataType("string");
		def3.setMatchTypeInternal(MatchTypes.FUZZY);
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

		String result = new StopWordUtility().getFieldDefinitionNamesWithStopwords(args);
		assertEquals("field2, field3", result);

	}
    
}
