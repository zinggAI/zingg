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



public enum FieldPreprocessor implements Serializable{
	
	NONE("NONE"),	
	CHINESE_COMPANY_STOP_WORD_REMOVER("CHINESE_COMPANY_STOP_WORD_REMOVER"),
	CHINESE_NAME_STOP_WORD_REMOVER("CHINESE_NAME_STOP_WORD_REMOVER"),
	MOBILE_STOP_WORD_REMOVER("MOBILE_STOP_WORD_REMOVER"),
	CAMERA_STOP_WORD_REMOVER("CAMERA_STOP_WORD_REMOVER"),
	THAI_COLOR_STOP_WORD_REMOVER("THAI_COLOR_STOP_WORD_REMOVER"),
	PUNCTUATION_CHARS_STOP_WORD_REMOVER("PUNCTUATION_CHARS_STOP_WORD_REMOVER"),
	ADDRESS_STOP_WORD_REMOVER("ADDRESS_STOP_WORD_REMOVER"),
	PERFUME_STOP_WORD_REMOVER("PERFUME_STOP_WORD_REMOVER"),
	FRENCH_COMPANY_STOP_WORD_REMOVER("FRENCH_COMPANY_STOP_WORD_REMOVER"),
	ENGLISH_COMPANY_STOP_WORD_REMOVER("ENGLISH_COMPANY_STOP_WORD_REMOVER"),
	AUTO_STOP_WORD_REMOVER("AUTO_STOP_WORD_REMOVER"),
	POWER_TOOLS_STOP_WORD_REMOVER("POWER_TOOLS_STOP_WORD_REMOVER"),
	DOMAIN_EXTRACTOR("DOMAIN_EXTRACTOR"),
	JAPANESE_COMPANY_STOP_WORD_REMOVER("JAPANESE_COMPANY_STOP_WORD_REMOVER"),
	JAPANESE_NAME_STOP_WORD_REMOVER("JAPANESE_NAME_STOP_WORD_REMOVER");
	
	private String value;
	private static Map<String, FieldPreprocessor> types;

	FieldPreprocessor(String type) {
		this.value = type;
	}

	private static void init() {
		types = new HashMap<String, FieldPreprocessor>();
		for (FieldPreprocessor f : FieldPreprocessor.values()) {
			types.put(f.value, f);
		}
	}

	@JsonValue
	public String value() {
		return value;
	}

	@JsonCreator
	public static FieldPreprocessor getFieldPreprocessor(String t) {
		if (types == null) {
			init();
		}
		return types.get(t);
	}

	public static Class getFieldPreprocessorClass(FieldPreprocessor c)
			throws ClassNotFoundException {
		return Class.forName(c.value());
	}


	

}
