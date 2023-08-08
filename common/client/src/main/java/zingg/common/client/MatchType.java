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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Field types used in defining the types of fields for matching. See the field
 * definitions and the user guide for more details
 */

public enum MatchType implements Serializable {
	/**
	 * Short words like first names and organizations with focus on first
	 * characters matching
	 */
	FUZZY("FUZZY"),

	/**
	 * Fields needing exact matches
	 */
	EXACT("EXACT"),

	
	/**
	 * Many times pin code is xxxxx-xxxx and has to be matched with xxxxx.
	 */
	PINCODE("PINCODE"),

	/**
	 * an email type which is supposed to look at only the first part of the email and ignore the domain.
	 */
	EMAIL("EMAIL"),

	/**
	 * Long descriptive text, usually more than a couple of words for example
	 * product descriptions
	 */
	TEXT("TEXT"),

	/**
	 * Strings containing numbers which need to be same. Example in addresses,
	 * we dont want 4th street to match 5th street
	 * Matching numbers with deviations
	 */
	NUMERIC("NUMERIC"),
	/*eg P301d, P00231*/
	NUMERIC_WITH_UNITS("NUMBER_WITH_UNITS"),
	NULL_OR_BLANK("NULL_OR_BLANK"),
	ONLY_ALPHABETS_EXACT("ONLY_ALPHABETS_EXACT"),
	ONLY_ALPHABETS_FUZZY("ONLY_ALPHABETS_FUZZY"),
	DONT_USE("DONT_USE");

	private String value;
	private static Map<String, MatchType> types;

	MatchType(String type) {
		this.value = type;
	}

	private static void init() {
		types = new HashMap<String, MatchType>();
		for (MatchType f : MatchType.values()) {
			types.put(f.value, f);
		}
	}

	@JsonCreator
	public static MatchType getMatchType(String t) throws ZinggClientException{
		if (types == null) {
			init();
		}
		MatchType type = types.get(t.trim().toUpperCase());
		if (type == null) throw new ZinggClientException("Unsupported Match Type: " + t);
		return type;
	}

	@JsonValue
	public String value() {
		return value;
	}

}
