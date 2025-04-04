package zingg.common.client.cols;

import java.util.List;

import zingg.common.client.Named;

public interface ISelectedCols {

    String[] getCols(List<? extends Named> n);

    String[] getCols();

    void setCols(List<String> cols);

    void setNamedCols(List<? extends Named> n);

}