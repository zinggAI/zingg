package zingg.common.core.block.data;

import zingg.common.core.util.IFromCsv;
import java.io.FileNotFoundException;
import java.util.List;

public interface ICsvReader extends DataReader {
    List<? extends IFromCsv> getRecords(String file, boolean skipHeader) throws FileNotFoundException;
}
