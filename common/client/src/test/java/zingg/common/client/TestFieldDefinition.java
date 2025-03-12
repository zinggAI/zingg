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
			List<IMatchType> matchType = Arrays.asList(MatchTypes.EMAIL, MatchTypes.FUZZY, MatchTypes.NULL_OR_BLANK);
			String expectedString = "EMAIL,FUZZY,NULL_OR_BLANK";
			String strMatchType = FieldDefinition.MatchTypeSerializer.getStringFromMatchType(matchType);
			assertEquals(expectedString, strMatchType);
		} catch (Exception | ZinggClientException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testConvertAListOFStringIntoMatchTypes() {
		try{
			String mtString = "FUZZY,NULL_OR_BLANK";
			List<IMatchType> expectedString = Arrays.asList(MatchTypes.FUZZY, MatchTypes.NULL_OR_BLANK);
			List<IMatchType> matchTypeString = FieldDefinition.MatchTypeDeserializer.getMatchTypeFromString(mtString);
			assertEquals(expectedString, matchTypeString);
		} catch (Exception | ZinggClientException e) {
			e.printStackTrace();
		}
	}
}
