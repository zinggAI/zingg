package zingg.common.client;

import java.io.Serializable;

public class FieldData implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String name;
	private String dataType;
	private Boolean nullable;

	public FieldData(String name, String dataType, Boolean nullable) {
		super();
		this.name = name;
		this.dataType = dataType;
		this.nullable = nullable;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public Boolean isNullable() {
		return nullable;
	}

	public Boolean getNullable() {
		return nullable;
	}

	public void setNullable(Boolean nullable) {
		this.nullable = nullable;
	}

}
