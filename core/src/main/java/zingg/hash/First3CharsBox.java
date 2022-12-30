package zingg.hash;

public abstract class First3CharsBox<D,R,C,T> extends HashFunction<D,R,C,T>{

	public First3CharsBox() {
		super("first3CharsBox");
		//, DataTypes.StringType, DataTypes.IntegerType, true);
	}

	
	
	//  @Override
	 public Integer call(String field) {
		 if (field == null || field.trim().length() <= 3) {
				return 0;
			} else {
				String sub = field.trim().toLowerCase().substring(0, 3);
				if (sub.compareTo("aaa") >= 0 && sub.compareTo("ezz") < 0) {
					return 1;
				} else if (sub.compareTo("ezz") >= 0 && sub.compareTo("izz") < 0) {
					return 2;
				} else if (sub.compareTo("izz") >= 0 && sub.compareTo("mzz") <= 0) {
					return 3;
				} else if (sub.compareTo("mzz") >= 0 && sub.compareTo("qzz") <= 0) {
					return 4;
				} else if (sub.compareTo("qzz") >= 0 && sub.compareTo("uzz") <= 0) {
					return 5;
				} else if (sub.compareTo("uzz") >= 0) {
					return 6;
				} else {
					return 7;
				}
			}//else
	 }
	 

}