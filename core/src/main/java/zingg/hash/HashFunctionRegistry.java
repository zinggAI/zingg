package zingg.hash;

import java.util.HashMap;

public class HashFunctionRegistry {

	public static HashMap<String, HashFunction> fns = new  HashMap<String,HashFunction>();
		
	public static HashFunction getFunction(String key) {
		return fns.get(key);
	}
	
	static {
		
		init(new IdentityString());
		init(new IdentityInteger());
		init(new First1Chars());
		init(new First2Chars());
		init(new First3Chars());
		init(new First4Chars());
		init(new Last1Chars());
		init(new Last2Chars());
		init(new Last3Chars());
		init(new Round());
		init(new LastWord());
		init(new First2CharsBox());
		init(new First3CharsBox());
		init(new IsNullOrEmpty());
	}
	
	public static void init(HashFunction fn) {
		fns.put(fn.getName(), fn);
	}
	
}
