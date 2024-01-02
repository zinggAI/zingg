package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.junit.jupiter.api.Test;

import zingg.common.core.hash.HashFnFromConf;

@JsonInclude(Include.NON_NULL)
public class TestHashFnFromConf {
  @Test
    public void testHashFnFromConf() {
        HashFnFromConf hashFnFromConf = new HashFnFromConf();
        hashFnFromConf.setName("Micheal");
        assertEquals("Micheal", hashFnFromConf.getName());
    }

    @Test
    public void testHashFnFromConf1() {
        HashFnFromConf hashFnFromConf = new HashFnFromConf();
        hashFnFromConf.setName(null);
        assertEquals(null, hashFnFromConf.getName());
    }
}