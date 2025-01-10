package zingg.common.core.util;

import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface IDataReader {
    List<String[]> readDataFromSource(String source) throws IOException, CsvException, URISyntaxException;
}
