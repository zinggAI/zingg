package zingg.similarity.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wcohen.ss.Jaccard;
import com.wcohen.ss.api.Token;
import com.wcohen.ss.api.Tokenizer;
import com.wcohen.ss.tokens.BasicToken;

public class BigramJaccard extends Jaccard implements Serializable {
	
	public static final Log LOG = LogFactory.getLog(BigramJaccard.class);

	
	public BigramJaccard() {
		this(new BigramTokenizer(true, true));
	}
	
	public BigramJaccard(Tokenizer t) {
		super(t);
	}
	
	
	public static class BigramTokenizer implements Tokenizer
	{
	    
	    private boolean ignorePunctuation = true;
	    private boolean ignoreCase = true;
		
	    public BigramTokenizer(boolean ignorePunctuation,boolean ignoreCase) {
	        this.ignorePunctuation = ignorePunctuation;
	        this.ignoreCase = ignoreCase;
	    }

	    // parameter setting
	    public void setIgnorePunctuation(boolean flag)  { ignorePunctuation = flag; }
	    public void setIgnoreCase(boolean flag)  { ignoreCase = flag; }
	    public String toString() { return "[BigramTokenizer "+ignorePunctuation+";"+ignoreCase+"]"; }
		
	    /**  Return tokenized version of a string.  Tokens are sequences
	     * of alphanumerics, or any single punctuation character. */
	    public Token[] tokenize(String input) 
	    {
	        List tokens = new ArrayList();
	        int cursor = 0;
	        input = input.replaceAll("\\s+", "");
	        LOG.debug("Input is " + input);
	        while (cursor<input.length()-2) {
	        	tokens.add(input.substring(cursor, cursor+1));
	        	cursor++;
	        }
	        return (Token[]) tokens.toArray(new BasicToken[tokens.size()]);
	    }
	    private Token internSomething(String s) 
	    {
	        return intern( ignoreCase ? s.toLowerCase() : s );
	    }
		
	    //
	    // 'interning' strings as tokens
	    //
	    private int nextId = 0;
	    private Map tokMap = new TreeMap();

	    public Token intern(String s) 
	    {
	        Token tok = (Token)tokMap.get(s);
	        if (tok==null) {
		    tok = new BToken(++nextId,s);
		    tokMap.put(s,tok);
	        }
	        return tok;
	    }

	    public Iterator tokenIterator()
	    {
	        return tokMap.values().iterator();
	    }

	    public int maxTokenIndex()
	    {
	        return nextId;
	    }
	}
	
	public static class BToken implements Token, Comparable
	{
		protected final int index;
		protected final String value;
		
		BToken(int index,String value) {
			this.index = index;
			this.value = value;
		}
		public String getValue() { return value; }
		public int getIndex() { return index; }
		public int compareTo(Object o) {
			Token t = (Token)o;
			return index - t.getIndex();
		} 
		public int hashCode() { return value.hashCode(); }
		public String toString() { return "[tok "+getIndex()+":"+getValue()+"]"; }
	}
}
