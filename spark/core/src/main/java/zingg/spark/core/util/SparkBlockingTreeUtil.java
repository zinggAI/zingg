/*
 * Zingg
 * Copyright (C) 2021-Present  Zingg Labs,inc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package zingg.spark.core.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.ml.util.SchemaUtils;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import zingg.common.client.ZFrame;
import zingg.common.client.util.ColName;
import zingg.common.client.util.ListMap;
import zingg.common.core.block.Block;
import zingg.common.core.block.Canopy;
import zingg.common.core.block.Tree;
import zingg.common.core.hash.HashFunction;
import zingg.common.core.util.BlockingTreeUtil;
import zingg.common.core.util.PipeUtilBase;
import zingg.spark.client.SparkFrame;
import zingg.spark.client.ZSparkSession;
import zingg.spark.core.block.SparkBlock;
import zingg.spark.core.block.SparkBlockFunction;

public class SparkBlockingTreeUtil extends BlockingTreeUtil<ZSparkSession, Dataset<Row>, Row, Column, DataType>{

    public static final Log LOG = LogFactory.getLog(SparkBlockingTreeUtil.class);
    protected ZSparkSession spark;
    
    public SparkBlockingTreeUtil(ZSparkSession s, PipeUtilBase pipeUtil) {
        this.spark = s;
        setPipeUtil(pipeUtil);
    }

    @Override
    public ZFrame<Dataset<Row>, Row, Column> getBlockHashes(ZFrame<Dataset<Row>, Row, Column> testData,
            Tree<Canopy<Row>> tree) {
            Dataset<Row> retDF = testData.df().map(new SparkBlockFunction(tree), RowEncoder.apply(
                    appendHashCol(testData.df().schema())));
            return new SparkFrame(retDF);
    }

    public StructType appendHashCol(StructType s) {
        StructType retSchema = SchemaUtils.appendColumn(s, ColName.HASH_COL, DataTypes.IntegerType, false);
        LOG.debug("returning schema after step 1 is " + retSchema);
        return retSchema;
}




@Override 
public ZFrame<Dataset<Row>, Row, Column> getTreeDF(byte[] blockingTree){
        StructType schema = DataTypes.createStructType(new StructField[] { DataTypes.createStructField("BlockingTree", DataTypes.BinaryType, false) });
        List<Row> objList = new ArrayList<>();
        objList.add(RowFactory.create(blockingTree));
        Dataset<Row> df = spark.getSession().sqlContext().createDataFrame(objList, schema).toDF().coalesce(1);
        return new SparkFrame(df);
}


/* 
@Override
public Tree<Canopy<Row>> readBlockingTree(Arguments args) throws Exception, ZinggClientException {
        PipeUtilBase<SparkSession, Dataset<Row>, Row, Column> pu = getPipeUtil();
        ZFrame<Dataset<Row>, Row, Column> tree = pu.read(false, 1, false, pu.getBlockingTreePipe(args));
        tree.show();
        tree.df().show();
        byte [] byteArrayBack = (byte[]) tree.df().head().get(0);
        Tree<Canopy<Row>> blockingTree = null;
        LOG.warn("byte array back is " + byteArrayBack);
        blockingTree =  (Tree<Canopy<Row>>) Util.revertObjectFromByteArray(byteArrayBack);
        return blockingTree;
}
*/

@Override
public Block<Dataset<Row>, Row, Column, DataType> getBlock(ZFrame<Dataset<Row>, Row, Column> sample,
                ZFrame<Dataset<Row>, Row, Column> positives,
                ListMap<DataType, HashFunction<Dataset<Row>, Row, Column, DataType>> hashFunctions, long blockSize) {
        // TODO Auto-generated method stub
        return new SparkBlock(sample, positives, hashFunctions, blockSize);
}
}