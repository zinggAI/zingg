package zingg.common.core.sink;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import zingg.common.core.sink.TableOutput;


public class TestTableOutput {

    private TableOutput getInstance() {
        return new TableOutput(3, 234456L, 87654L, "Company X");
    }

    @Test
    public void testGetMethods() {
        String ans = "Company X";
        TableOutput value = getInstance();
        assertEquals(3, value.getJobId());
        assertEquals(234456L, value.getTimestamp());
        assertEquals(87654L, value.getClusterId());
        assertEquals(ans, value.getRecord());
    }

    @Test
    public void testSetMethods() {
        TableOutput value = getInstance();
        int newJobId = 5;
        long newTimestamp = 778899L;
        long newClusterId = 9876L;
        String newRecord = "Company Y";

        value.setJobId(newJobId);
        value.setTimestamp(newTimestamp);
        value.setClusterId(newClusterId);
        value.setRecord(newRecord);

        assertEquals(5, value.getJobId());
        assertEquals(778899L, value.getTimestamp());
        assertEquals(9876L, value.getClusterId());
        assertEquals(newRecord, value.getRecord());
    }

}
