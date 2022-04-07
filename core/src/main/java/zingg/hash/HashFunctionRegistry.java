package zingg.hash;

public interface HashFunctionRegistry<D,R,C,T,T1>  {

	//public static HashMap<String, HashFunction<D,R,C,T,T1> > fns = new  HashMap<String,HashFunction<D,R,C,T,T1>>();
		
	public HashFunction<D,R,C,T,T1> getFunction(String key);
	
	public void init(HashFunction<D,R,C,T,T1> fn);
	
}
