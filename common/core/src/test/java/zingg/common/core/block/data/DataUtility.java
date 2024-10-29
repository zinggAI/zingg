package zingg.common.core.block.data;

import com.opencsv.exceptions.CsvException;
import zingg.common.core.block.model.Customer;
import zingg.common.core.block.model.CustomerDupe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataUtility {

    private final DataReader dataReader;

    public DataUtility(DataReader dataReader) {
        this.dataReader = dataReader;
    }

    public List<CustomerDupe> getCustomerDupes(String source, boolean varianceAdded) throws IOException, CsvException {

        List<CustomerDupe> testCustomerDupes = new ArrayList<>();

        List<String[]> allData = dataReader.readDataFromSource(source);
        for (String[] row : allData) {
            String[] dupe = new String[2 * row.length];
            System.arraycopy(row, 0, dupe, 0, row.length);
            String[] sideRow;
            if (varianceAdded) {
                sideRow = getVarianceAddedRow(row);
            } else {
                sideRow = getNonVarianceAddedRow(row);
            }
            System.arraycopy(sideRow, 0, dupe, sideRow.length, sideRow.length);
            testCustomerDupes.add(new CustomerDupe(dupe));
        }
        return testCustomerDupes;
    }


    public List<Customer> getCustomers(String source) throws IOException, CsvException {

        List<Customer> testCustomers = new ArrayList<>();

        List<String[]> allData = dataReader.readDataFromSource(source);
        for (String[] row : allData) {
            testCustomers.add(new Customer(row));
        }
        return testCustomers;
    }

    private String[] getVarianceAddedRow(String[] row) {
        String[] varianceAddedRow = new String[row.length];
        varianceAddedRow[0] = row[0];
        for(int idx = 1; idx < row.length; idx++) {
            varianceAddedRow[idx] = "v_" + row[idx] + "_v";
        }

        return varianceAddedRow;
    }

    private String[] getNonVarianceAddedRow(String[] row) {
        return row;
    }
}
