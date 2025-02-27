package zingg.common.core.util;

import zingg.common.client.ZFrame;
import zingg.common.client.util.ColName;

import java.util.ArrayList;
import java.util.List;

public class LabellerUtil<D, R, C> {

    public ZFrame<D,R,C> postProcessLabel(ZFrame<D,R,C> updatedLabelledRecords, ZFrame<D,R,C> unmarkedRecords) {
        List<C> cols = new ArrayList<C>();
        cols.add(updatedLabelledRecords.col(ColName.ID_COL));
        cols.add(updatedLabelledRecords.col(ColName.CLUSTER_COLUMN));

        String[] unmarkedRecordColumns = unmarkedRecords.columns();

        //drop isMatch column from unMarked records
        //and replace with updated isMatch column
        cols.add(updatedLabelledRecords.col(ColName.MATCH_FLAG_COL));
        ZFrame<D,R,C> zFieldsFromUpdatedLabelledRecords = updatedLabelledRecords.select(cols).
                withColumnRenamed(ColName.ID_COL, ColName.COL_PREFIX + ColName.ID_COL).
                withColumnRenamed(ColName.CLUSTER_COLUMN, ColName.COL_PREFIX + ColName.CLUSTER_COLUMN);

        unmarkedRecords = unmarkedRecords.drop(ColName.MATCH_FLAG_COL);

		/*
			join on z_id and z_cluster
		 */
        C joinCondition1 = unmarkedRecords.equalTo(unmarkedRecords.col(ColName.ID_COL), zFieldsFromUpdatedLabelledRecords.col(ColName.COL_PREFIX + ColName.ID_COL));
        C joinCondition2 = unmarkedRecords.equalTo(unmarkedRecords.col(ColName.CLUSTER_COLUMN), zFieldsFromUpdatedLabelledRecords.col(ColName.COL_PREFIX + ColName.CLUSTER_COLUMN));
        C joinCondition = unmarkedRecords.and(joinCondition1, joinCondition2);

        //we are selecting columns to bring back to original shape
        return unmarkedRecords.join(zFieldsFromUpdatedLabelledRecords, joinCondition, ZFrame.INNER_JOIN).select(unmarkedRecordColumns);
    }
}
