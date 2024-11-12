package zingg.common.client.util.verticalDisplay;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.client.util.Pair;

import java.util.ArrayList;
import java.util.List;

public abstract class VerticalDisplayUtility<S, D, R, C> {

    private final DFObjectUtil<S, D, R, C> dfObjectUtil;

    public VerticalDisplayUtility(DFObjectUtil<S, D, R, C> dfObjectUtil) {
        this.dfObjectUtil = dfObjectUtil;
    }

    public void showVertical(ZFrame<D, R, C> zFrame) throws ZinggClientException {
        ZFrame<D, R, C> verticalZFrame = convertVertical(zFrame);
        verticalZFrame.show(1000);
    }

    public ZFrame<D, R, C> convertVertical(ZFrame<D, R, C> zFrame) throws ZinggClientException {
        try {
            String[] columns = zFrame.columns();
            List<Pair<String, String>> comparison_pairs = getComparisonPairs(zFrame);
            List<VerticalDisplayModel> rowList = getList(comparison_pairs, columns);
            return dfObjectUtil.getDFFromObjectList(rowList, VerticalDisplayTwoRowModel.class);
        } catch (Exception exception) {
            throw new ZinggClientException("Error occurred while converting to vertical, " + exception.getMessage());
        }
    }

    private List<VerticalDisplayModel> getList(List<Pair<String, String>> comparison_pairs, String[] columns) {
        List<VerticalDisplayModel> samples = new ArrayList<>();
        for (int idx = 0; idx < comparison_pairs.size(); idx++){
            samples.add(new VerticalDisplayTwoRowModel(columns[idx], comparison_pairs.get(idx).getFirst(), comparison_pairs.get(idx).getSecond()));
        }

        return samples;
    }

    protected abstract List<Pair<String, String>> getComparisonPairs(ZFrame<D, R, C> zFrame);
}
