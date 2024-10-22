package zingg.common.core.hash;

public class HashFnBoolean extends BaseHash<Boolean,Integer>{

	public HashFnBoolean() {
	    setName("HashFnBoolean");
	}

    public Integer call(Boolean field){
        return Boolean.hashCode(field); // 1231 for true and 1237 for false 
    }
    
}
