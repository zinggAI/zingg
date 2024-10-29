package zingg.common.core.block.data;

import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.util.List;

public interface DataReader {
    List<String[]> readDataFromSource(String source) throws IOException, CsvException;
}
