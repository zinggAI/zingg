package zingg.hash;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class BaseHash<T1, R> implements Serializable{
	
	public static final Log LOG = LogFactory.getLog(BaseHash.class);

	private String name;
	
	public abstract R call(T1 t1);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
