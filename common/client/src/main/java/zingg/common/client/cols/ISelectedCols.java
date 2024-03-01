package zingg.common.client.cols;

import java.util.List;

public interface ISelectedCols {

    String[] getCols(List<? extends Named> n);

    String[] getCols();

    void setCols(String[] cols);
    
    void setCols(List<String> cols);
}