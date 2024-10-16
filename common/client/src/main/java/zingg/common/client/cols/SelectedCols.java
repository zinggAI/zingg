package zingg.common.client.cols;

import java.util.List;

public class SelectedCols implements ISelectedCols {

    private String[] cols;

    @Override
    public String[] getCols(List<? extends Named> n) {
        String[] result = new String[n.size()];
        for (int i = 0; i < n.size(); i++) {
            result[i] = n.get(i).getName();
        }
        return result;
    }

    @Override
    public String[] getCols() {
        return cols;
    }

    @Override
    public void setCols(List<String> strings) {
        this.cols = strings.toArray(new String[0]);
    }

    @Override
    public void setNamedCols(List<? extends Named> n) {
        this.cols = getCols(n);
    }

    @Override
    public void setStringCols(List<String> columnNames) {
        this.cols = columnNames.toArray(new String[0]);
    }
}