package zingg.stat;

import java.io.Serializable;

import zingg.client.MatchType;

public interface Stat<E> extends Serializable {
	MatchType getFieldType();

	public void setFieldType(MatchType f);

}
