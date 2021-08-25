/*
 * Copyright Nube Technologies 2014
 */

package zingg.client;

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
	 * Short words like last name or full names with focus on last characters
	 * matching
	 */
	FUZZY_LAST("FUZZY_LAST"),

	FUZZY_GARBLED("FUZZY_GARBLED"),

	/**
	 * Fields needing exact matches
	 */
	EXACT("EXACT"),

	/**
	 * Long descriptive text, usually more than a couple of words for example
	 * product descriptions
	 */
	TEXT("TEXT"),

	/**
	 * Strings containing numbers which need to be same. Example in addresses,
	 * we dont want 4th street to match 5th street
	 */
	ALPHANUMERIC("ALPHANUMERIC"),

	/**
	 * All numbers which are numerically near each other but may not be exact
	 * matches to go here
	 */
	NUMERIC("NUMERIC"),

	/**
	 * Composite field is a field made by aggregating or compsing multiple
	 * fields into one Used in places where fields may be swapped
	 */
	COMPOSITE("COMPOSITE"),
	ALPHANUMERIC_WITH_UNITS("ALPHANUMERIC_WITH_UNITS"),
	DONT_USE("DONT USE");

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
	public static MatchType getMatchType(String t) {
		if (types == null) {
			init();
		}
		return types.get(t.toUpperCase());
	}

	@JsonValue
	public String value() {
		return value;
	}

}
