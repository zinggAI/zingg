package zingg.common.client.relate;

import java.util.HashMap;
import java.util.Map;

public class VertexTypes {
    public final static IVertexType ZINGG_PIPE = new VertexType("zingg_pipe");
    public static Map<String, IVertexType> allVertices;

    

    protected VertexTypes() {
    }

    public static final void put(IVertexType o) {
        if (allVertices == null) {
            allVertices = new HashMap<String, IVertexType>();
        }
        allVertices.put(o.getName(), o);
    }
    
    
    
    public static String[] getAll() {
        IVertexType[] zo = allVertices.values().toArray(new IVertexType[allVertices.size()]);
        int i = 0;
        String[] s = new String[zo.length];
        for (IVertexType z: zo) {
            s[i++] = z.getName();
        }
        return s;
    }

   
    public static final IVertexType getByValue(String value){
        for (IVertexType zo: VertexTypes.allVertices.values()) {
            if (zo.getName().equals(value)) return zo;
        }
        return null;
    }

	
}