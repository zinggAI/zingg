/*
 * Zingg
 * Copyright (C) 2021-Present  Zingg Labs,inc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
			List<MatchType> matchType = Arrays.asList(MatchType.EMAIL, MatchType.FUZZY, MatchType.NULL_OR_BLANK);
			String expectedString = "EMAIL,FUZZY,NULL_OR_BLANK";
			String strMatchType = FieldDefinition.MatchTypeSerializer.getStringFromMatchType(matchType);
			assertEquals(expectedString, strMatchType);
		} catch (Exception | ZinggClientException e) {
			e.printStackTrace();
		}
	}
}
