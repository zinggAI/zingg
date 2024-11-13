package zingg.common.client.util.verticalDisplay;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.DFObjectUtil;

import java.util.ArrayList;
import java.util.List;

public class VerticalDisplayUtility<S, D, R, C> {

    private final DFObjectUtil<S, D, R, C> dfObjectUtil;
    private static final int MAX_COLUMNS = 1000;

    public VerticalDisplayUtility(DFObjectUtil<S, D, R, C> dfObjectUtil) {
        this.dfObjectUtil = dfObjectUtil;
    }

    public void showVertical(ZFrame<D, R, C> zFrame) throws ZinggClientException {
        ZFrame<D, R, C> verticalZFrame = convertVertical(zFrame);
        verticalZFrame.show(MAX_COLUMNS);
    }

    public ZFrame<D, R, C> convertVertical(ZFrame<D, R, C> zFrame) throws ZinggClientException {
        try {
            String[] columns = zFrame.columns();
            ZFrame<D, R, C> zFrame1 = zFrame.limit(1);
            ZFrame<D, R, C> zFrame2 = zFrame.except(zFrame1);

            R row1 = zFrame.head();
            R row2 = zFrame2.head();
            //iterate through all the columns
            //and create model list
            List<VerticalDisplayModel> samples = new ArrayList<>();
            for (String column : columns) {
                samples.add(new VerticalDisplayTwoRowModel(column, getString(zFrame.getAsString(row1, column)),
                        getString(zFrame2.getAsString(row2, column))));
            }
            return dfObjectUtil.getDFFromObjectList(samples, VerticalDisplayTwoRowModel.class);
        } catch (Exception exception) {
            throw new ZinggClientException("Error occurred while converting to vertical, " + exception.getMessage());
        }
    }

    private String getString(Object object) {
        return object == null ? null : object.toString();
    }

}
