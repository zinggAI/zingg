package zingg.common.client;

import java.util.HashMap;
import java.util.Map;

public class MatchTypes {

    public final static IMatchType FUZZY = new MatchType("FUZZY");
    public final static IMatchType EXACT = new MatchType("EXACT");
    public final static IMatchType PINCODE = new MatchType("PINCODE");
    public final static IMatchType EMAIL = new MatchType("EMAIL");
    public final static IMatchType TEXT = new MatchType("TEXT");
    public final static IMatchType NUMERIC = new MatchType("NUMERIC");
    public final static IMatchType NUMERIC_WITH_UNITS = new MatchType("NUMERIC_WITH_UNITS");
    public final static IMatchType NULL_OR_BLANK = new MatchType("NULL_OR_BLANK");
    public final static IMatchType ONLY_ALPHABETS_EXACT = new MatchType("ONLY_ALPHABETS_EXACT");
    public final static IMatchType ONLY_ALPHABETS_FUZZY = new MatchType("ONLY_ALPHABETS_FUZZY");
    public final static IMatchType DONT_USE = new MatchType("DONT_USE");

    public static Map<String, IMatchType> allMatchTypes;// = new HashMap<String, IMatchType>(); 

    protected MatchTypes(){

    }

    public static final void put(IMatchType o) { 

        if (allMatchTypes == null) { 
            allMatchTypes = new HashMap<String, IMatchType>(); 
        } 

        allMatchTypes.put(o.getName().toUpperCase(), o);  
    } 
        
    public static String[] getAllMatchTypes() { 
        IMatchType[] zo = allMatchTypes.values().toArray(new IMatchType[allMatchTypes.size()]); 
        int i = 0; 
        String[] s = new String[zo.length]; 
        for (IMatchType z: zo) { 
            s[i++] = z.getName(); 
        } 
        return s; 
    } 

    public static IMatchType getByName(String name) throws Exception{ 
        for (IMatchType zo: MatchTypes.allMatchTypes.values()) { 
            if (zo.getName().equalsIgnoreCase(name)) {
                return zo; 
            }
        } 
        return null; 
    } 
    
}
