package zingg.common.core.util;

import java.io.FileNotFoundException;
import java.util.List;

public interface ICsvReader extends IDataReader {
    List<? extends IFromCsv> getRecords(String file, boolean skipHeader) throws FileNotFoundException;
}
