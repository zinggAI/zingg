package zingg.common.client;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;

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
