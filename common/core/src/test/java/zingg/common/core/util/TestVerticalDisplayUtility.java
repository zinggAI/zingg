package zingg.common.core.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import zingg.common.client.ZFrame;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.client.util.VerticalDisplayUtility;

import java.util.ArrayList;
import java.util.List;

public abstract class TestVerticalDisplayUtility<S, D, R, C> {

    private final DFObjectUtil<S, D, R, C> dfObjectUtil;

    public TestVerticalDisplayUtility(DFObjectUtil<S, D, R, C> dfObjectUtil) {
        this.dfObjectUtil = dfObjectUtil;
    }

    @Test
    public void testWithoutNulls() throws Exception {
        VerticalDisplayUtility<D, R, C> verticalDisplayUtility = getVerticalDisplayUtility();
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(getDataWithoutNulls(), Customer.class);

        Assertions.assertDoesNotThrow(() -> verticalDisplayUtility.showVertical(zFrame.df()));
    }

    @Test
    public void testWithNulls() throws Exception {
        VerticalDisplayUtility<D, R, C> verticalDisplayUtility = getVerticalDisplayUtility();
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(getDataWithNulls(), Customer.class);

        Assertions.assertDoesNotThrow(() -> verticalDisplayUtility.showVertical(zFrame.df()));
    }

    @Test
    public void testWithFullRowNullExceptPrimaryKey() throws Exception {
        VerticalDisplayUtility<D, R, C> verticalDisplayUtility = getVerticalDisplayUtility();
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(getDataWithFullRowNullExceptPrimaryKey(), Customer.class);

        Assertions.assertDoesNotThrow(() -> verticalDisplayUtility.showVertical(zFrame.df()));
    }

    @Test
    public void testWithFullRowNull() throws Exception {
        VerticalDisplayUtility<D, R, C> verticalDisplayUtility = getVerticalDisplayUtility();
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(getDataWithFullRowNull(), Customer.class);

        Assertions.assertDoesNotThrow(() -> verticalDisplayUtility.showVertical(zFrame.df()));
    }




    public static List<Customer> getDataWithoutNulls() {
        List<Customer> sample = new ArrayList<Customer>();
        sample.add(new Customer("Nitish", "Joshi", "123", "0000", "1"));
        sample.add(new Customer("Nitis", "Joshi", "123", "0000", "2"));
        return sample;
    }

    public static List<Customer> getDataWithNulls() {
        List<Customer> sample = new ArrayList<Customer>();
        sample.add(new Customer("Nitish", "Joshi", "123", null, "1"));
        sample.add(new Customer("Nitis", null, "123", "0000", "2"));
        return sample;
    }

    public static List<Customer> getDataWithFullRowNullExceptPrimaryKey() {
        List<Customer> sample = new ArrayList<Customer>();
        sample.add(new Customer("Nitish", "Joshi", "123", null, "1"));
        sample.add(new Customer(null, null, null, null, "2"));
        return sample;
    }

    public static List<Customer> getDataWithFullRowNull() {
        List<Customer> sample = new ArrayList<Customer>();
        sample.add(new Customer("Nitish", "Joshi", "123", null, "1"));
        sample.add(new Customer(null, null, null, null, null));
        return sample;
    }


    public static class Customer {
        public final String fname;
        public final String lname;
        public final String ssn;
        public final String dob;
        public final String row_uuid;

        public Customer(String fname, String lname, String ssn, String dob, String row_uuid) {
            this.fname = fname;
            this.lname = lname;
            this.ssn = ssn;
            this.dob = dob;
            this.row_uuid = row_uuid;
        }
    }

    protected abstract VerticalDisplayUtility<D, R, C> getVerticalDisplayUtility();
}
