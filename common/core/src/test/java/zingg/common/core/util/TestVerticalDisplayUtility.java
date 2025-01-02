package zingg.common.core.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.client.util.vertical.VerticalDisplayUtility;

import java.util.ArrayList;
import java.util.List;

public abstract class TestVerticalDisplayUtility<S, D, R, C> {

    private final DFObjectUtil<S, D, R, C> dfObjectUtil;

    public TestVerticalDisplayUtility(DFObjectUtil<S, D, R, C> dfObjectUtil) {
        this.dfObjectUtil = dfObjectUtil;
    }

    @Test
    public void testWithoutNulls() throws Exception, ZinggClientException {
        VerticalDisplayUtility<S, D, R, C> verticalDisplayUtility = new VerticalDisplayUtility<S, D, R, C>(dfObjectUtil);
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(getDataWithoutNulls(), Customer.class);
        ZFrame<D, R, C> zFrameVertical = verticalDisplayUtility.convertVertical(zFrame);

        //Assert on total number of rows
        Assertions.assertEquals(5, zFrameVertical.count(), "Count is not as expected");

        //Assert on first row
        R row = zFrameVertical.head();
        Assertions.assertEquals("fname", zFrameVertical.getAsString(row, "Field"));
        Assertions.assertEquals("Nitish", zFrameVertical.getAsString(row, "Value1"));
        Assertions.assertEquals("Nitis", zFrameVertical.getAsString(row, "Value2"));
    }

    @Test
    public void testWithNulls() throws Exception, ZinggClientException {
        VerticalDisplayUtility<S, D, R, C> verticalDisplayUtility = new VerticalDisplayUtility<S, D, R, C>(dfObjectUtil);
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(getDataWithNulls(), Customer.class);
        ZFrame<D, R, C> zFrameVertical = verticalDisplayUtility.convertVertical(zFrame);

        //Assert on total number of rows
        Assertions.assertEquals(5, zFrameVertical.count(), "Count is not as expected");

        //Assert on first row
        R row = zFrameVertical.head();
        Assertions.assertEquals("fname", zFrameVertical.getAsString(row, "Field"));
        Assertions.assertEquals("Nitish", zFrameVertical.getAsString(row, "Value1"));
        Assertions.assertNull(zFrameVertical.getAsString(row, "Value2"));
    }

    @Test
    public void testWithFullRowNullExceptPrimaryKey() throws Exception, ZinggClientException {
        VerticalDisplayUtility<S, D, R, C> verticalDisplayUtility = new VerticalDisplayUtility<S, D, R, C>(dfObjectUtil);
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(getDataWithFullRowNullExceptPrimaryKey(), Customer.class);
        ZFrame<D, R, C> zFrameVertical = verticalDisplayUtility.convertVertical(zFrame);

        //Assert on total number of rows
        Assertions.assertEquals(5, zFrameVertical.count(), "Count is not as expected");

        //Assert on first row
        R row = zFrameVertical.head();
        Assertions.assertEquals("fname", zFrameVertical.getAsString(row, "Field"));
        Assertions.assertEquals("Nitish", zFrameVertical.getAsString(row, "Value1"));
        Assertions.assertNull(zFrameVertical.getAsString(row, "Value2"));
    }

    @Test
    public void testWithFullRowNull() throws Exception, ZinggClientException {
        VerticalDisplayUtility<S, D, R, C> verticalDisplayUtility = new VerticalDisplayUtility<S, D, R, C>(dfObjectUtil);
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(getDataWithFullRowNull(), Customer.class);
        ZFrame<D, R, C> zFrameVertical = verticalDisplayUtility.convertVertical(zFrame);

        //Assert on total number of rows
        Assertions.assertEquals(5, zFrameVertical.count(), "Count is not as expected");

        //Assert on first row
        R row = zFrameVertical.head();
        Assertions.assertEquals("fname", zFrameVertical.getAsString(row, "Field"));
        Assertions.assertEquals("Nitish", zFrameVertical.getAsString(row, "Value1"));
        Assertions.assertNull(zFrameVertical.getAsString(row, "Value2"));
    }

    @Test
    public void testWithEmptyFrame() throws Exception, ZinggClientException {
        VerticalDisplayUtility<S, D, R, C> verticalDisplayUtility = new VerticalDisplayUtility<S, D, R, C>(dfObjectUtil);
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(getEmptyData(), Customer.class);
        ZFrame<D, R, C> zFrameVertical = verticalDisplayUtility.convertVertical(zFrame);

        //Assert on total number of rows
        Assertions.assertEquals(0, zFrameVertical.count(), "Count is not as expected");
    }

    @Test
    public void testWithNullFrame() throws ZinggClientException {
        VerticalDisplayUtility<S, D, R, C> verticalDisplayUtility = new VerticalDisplayUtility<S, D, R, C>(dfObjectUtil);
        ZFrame<D, R, C> zFrameVertical = verticalDisplayUtility.convertVertical(null);

        //Assert on total number of rows
        Assertions.assertEquals(0, zFrameVertical.count(), "Count is not as expected");
    }

    @Test
    public void testWithIdenticalRows() throws ZinggClientException, Exception {
        VerticalDisplayUtility<S, D, R, C> verticalDisplayUtility = new VerticalDisplayUtility<S, D, R, C>(dfObjectUtil);
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(getDataWithIdenticalRows(), Customer.class);
        ZFrame<D, R, C> zFrameVertical = verticalDisplayUtility.convertVertical(zFrame);

        //Assert on total number of rows
        Assertions.assertEquals(5, zFrameVertical.count(), "Count is not as expected");
    }

    public static List<Customer> getEmptyData() {
        return new ArrayList<Customer>();
    }

    public static List<Customer> getDataWithIdenticalRows() {
        List<Customer> sample = new ArrayList<Customer>();
        sample.add(new Customer("Nitish", "Joshi", "123", "0000", "1"));
        sample.add(new Customer("Nitish", "Joshi", "123", "0000", "1"));
        return sample;
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
        sample.add(new Customer(null, null, "123", "0000", "2"));
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

}
