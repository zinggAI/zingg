package zingg.common.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

public class TestFieldDefinition {
	public static final Log LOG = LogFactory.getLog(TestFieldDefinition.class);

	@Test
	public void testConvertAListOFMatchTypesIntoString() {
		try {
			List<? extends IMatchType> matchType = Arrays.asList(MatchTypes.EMAIL, MatchTypes.FUZZY, MatchTypes.NULL_OR_BLANK);
			String expectedString = "EMAIL,FUZZY,NULL_OR_BLANK";
			String strMatchType = FieldDefinition.MatchTypeSerializer.getStringFromMatchType(matchType);
			assertEquals(expectedString, strMatchType);
		} catch (Exception | ZinggClientException e) {
			e.printStackTrace();
		}
	}
}
