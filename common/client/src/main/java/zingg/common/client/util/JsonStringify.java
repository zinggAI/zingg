package zingg.common.client.util;

import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import zingg.common.client.Arguments;
import zingg.common.client.ArgumentsUtil;

public class JsonStringify {
	public static String toString (Object o){
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
		//mapper.configure(JsonParser.Feature.FAIL_ON_EMPTY_BEANS, true)
		try {
			StringWriter writer = new StringWriter();
			return mapper.writeValueAsString(o);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
        }
    }

}
