package zingg.common.core.hash;

public class Round extends BaseHash<Double,Long>{
	
	public Round() {
	    setName("round");
	}
	
	public Long call(Double field) {
		 return field == null ? null : Math.round(field);
	}

}


