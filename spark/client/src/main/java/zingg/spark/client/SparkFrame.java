package zingg.spark.client;

import java.util.List;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;

import scala.collection.JavaConverters;
import zingg.common.client.*;
import zingg.common.client.util.ColName;

//Dataset, Row, column
public class SparkFrame implements ZFrame<Dataset<Row>, Row, Column> {

	public Dataset<Row> df;

    public SparkFrame(Dataset<Row> df) {
        this.df = df;
    }

    public Dataset<Row> df() {
        return df;
    }
    
    public ZFrame<Dataset<Row>, Row, Column> cache() {
        return new SparkFrame(df.cache());
    }

    public ZFrame<Dataset<Row>, Row, Column> as(String s) {
        return new SparkFrame(df.as(s));
    }


    public String[] columns() {
        return df.columns();
    }

    public ZFrame<Dataset<Row>, Row, Column> select(Column... cols) {
        return new SparkFrame(df.select(cols));
    }

    
    public ZFrame<Dataset<Row>, Row, Column> select(List<Column> cols){
        return new SparkFrame(df.select(JavaConverters.asScalaIteratorConverter(cols.iterator()).asScala().toSeq()));
    }
    
    
    public ZFrame<Dataset<Row>, Row, Column> select(String col) {
        return new SparkFrame(df.select(col));
    }

    public ZFrame<Dataset<Row>, Row, Column> selectExpr(String... col) {
        return new SparkFrame(df.selectExpr(col));
    }

    public ZFrame<Dataset<Row>, Row, Column> select(String col, String... col1) {
        return new SparkFrame(df.select(col, col1));
    }

    public ZFrame<Dataset<Row>, Row, Column> distinct() {
        return new SparkFrame(df.distinct());
    }

    public List<Row> collectAsList() {
        return df.collectAsList();
    }

    public List<String> collectAsListOfStrings() {
        return df.as(Encoders.STRING()).collectAsList();
    }
    
    public ZFrame<Dataset<Row>, Row, Column> toDF(String[] cols) {
        return new SparkFrame(df.toDF(cols));
    }

    public ZFrame<Dataset<Row>, Row, Column> toDF(String col1, String col2) {
        return new SparkFrame(df.toDF(col1, col2));
    }
    
    public ZFrame<Dataset<Row>, Row, Column> join(ZFrame<Dataset<Row>, Row, Column> lines1, String joinColumn) {
        return new SparkFrame(df.join(lines1.df(), df.col(joinColumn).equalTo(lines1.df().col(ColName.COL_PREFIX + joinColumn))));
    }

    public ZFrame<Dataset<Row>, Row, Column> joinOnCol(ZFrame<Dataset<Row>, Row, Column> lines1, String joinColumn) {
        return new SparkFrame(df.join(lines1.df(), joinColumn));
    }

    public ZFrame<Dataset<Row>, Row, Column> join(ZFrame<Dataset<Row>, Row, Column> lines1, String joinColumn1, String joinColumn2){
        return new SparkFrame(df.join(lines1.df(), 
            df.col(joinColumn1).equalTo(lines1.df().col(joinColumn1)).and(df.col(joinColumn2).equalTo(lines1.df().col(joinColumn2)))));
    }

    public ZFrame<Dataset<Row>, Row, Column> joinRight(ZFrame<Dataset<Row>, Row, Column> lines1, String joinColumn) {
        return join(lines1, joinColumn, false, "right");
    }

    public ZFrame<Dataset<Row>, Row, Column> join(ZFrame<Dataset<Row>, Row, Column> lines1, String joinColumn, boolean addPrefixToCol, String joinType) {
        String joinColumn1 = joinColumn;
        joinColumn1 = (addPrefixToCol ? ColName.COL_PREFIX + joinColumn1 : joinColumn1);
        return new SparkFrame(df.join(lines1.df(), df.col(joinColumn).equalTo(lines1.df().col(joinColumn1)), joinType));

    }

    public Column col(String colName) {
        return df.col(colName);
    }

    public long count() {
        return df.count();
    }

    public ZFrame<Dataset<Row>, Row, Column> filter(Column col) {
        return new SparkFrame(df.filter(col));
    }

    public ZFrame<Dataset<Row>, Row, Column> withColumnRenamed(String s, String t) {
        return new SparkFrame(df.withColumnRenamed(s, t));

    }

    public ZFrame<Dataset<Row>, Row, Column> dropDuplicates(String c, String... d) {
        return new SparkFrame(df.dropDuplicates(c, d));
    }

    public ZFrame<Dataset<Row>, Row, Column> drop(String c) {
        return new SparkFrame(df.drop(c));
    }

    public ZFrame<Dataset<Row>, Row, Column> except(ZFrame<Dataset<Row>, Row, Column> c) {
        return new SparkFrame(df.except(c.df()));
    }

    @Override
    public double aggSum(String colName) {
    	return df.agg(functions.sum(colName).cast("double")).collectAsList().get(0).getDouble(0);
    }

    public ZFrame<Dataset<Row>, Row, Column> groupByMinMax(Column c) {
        return new SparkFrame(df.groupBy(df.col(ColName.ID_COL)).agg(
			functions.min(ColName.SCORE_COL).as(ColName.SCORE_MIN_COL),
			functions.max(ColName.SCORE_COL).as(ColName.SCORE_MAX_COL)));
    }

    @Override
    public ZFrame<Dataset<Row>, Row, Column> groupByCount(String colName, String countColName){
    	return new SparkFrame(df.groupBy(colName).count().withColumnRenamed("count",countColName));
    }
    
    public ZFrame<Dataset<Row>, Row, Column> dropDuplicates(String[] c) {
        return new SparkFrame(df.dropDuplicates(c));
    }


    public ZFrame<Dataset<Row>, Row, Column> union(ZFrame<Dataset<Row>, Row, Column> other) {
        return new SparkFrame(df.union(other.df()));
    }

    public ZFrame<Dataset<Row>, Row, Column> unionByName(ZFrame<Dataset<Row>, Row, Column> other, boolean flag) {
        return new SparkFrame(df.unionByName(other.df(), flag));
    }

    public ZFrame<Dataset<Row>, Row, Column> withColumn(String s, int c){
        return new SparkFrame(df.withColumn(s, functions.lit(c)));
    }
    public ZFrame<Dataset<Row>, Row, Column> withColumn(String s, double c){
        return new SparkFrame(df.withColumn(s, functions.lit(c)));
    }

    public ZFrame<Dataset<Row>, Row, Column> withColumn(String s, String c){
        return new SparkFrame(df.withColumn(s, functions.lit(c)));
    }

    public ZFrame<Dataset<Row>, Row, Column> repartition(int nul){
        return new SparkFrame(df.repartition(nul));
    }

    public ZFrame<Dataset<Row>, Row, Column> repartition(int nul, Column c){
        return new SparkFrame(df.repartition(nul, c));
    }

    public Column gt(String c) {
		return df.col(c).gt(df.col(ColName.COL_PREFIX + c));
	}
    
    @Override
    public Column gt(String c, double val) {
		return df.col(c).gt(val);
	}
    
	public Column equalTo(String c, String e){
		return df.col(c).equalTo(e);
	}

	public Column notEqual(String c, String e) {
		return df.col(c).notEqual(e);
	}

    public Column equalTo(String c, int e){
		return df.col(c).equalTo(e);
	}

    public Column equalTo(String c, double e){
		return df.col(c).equalTo(e);
	}

	public Column notEqual(String c, int e) {
		return df.col(c).notEqual(e);
	}

    public ZFrame<Dataset<Row>, Row, Column> sample(boolean withReplacement, float num){
        return new SparkFrame(df.sample(withReplacement, num));
    }

    public ZFrame<Dataset<Row>, Row, Column> sample(boolean withReplacement, double num){
        return new SparkFrame(df.sample(withReplacement, num));
    }

    public ZFrame<Dataset<Row>, Row, Column> coalesce(int num){
        return new SparkFrame(df.coalesce(num));
    }

    public void show(int num) {
        df.show(num);
    }

    public void show() {
        df.show();
    }

    @Override
    public ZFrame<Dataset<Row>, Row, Column> drop(String... c) {
        return new SparkFrame(df.drop(c));
    }

    public ZFrame<Dataset<Row>, Row, Column> withColumn(String s,Column c) {
        return new SparkFrame(df.withColumn(s,c));

    }

    public Column concat(Column s,Column c) {
        return functions.concat(s,c);

    }

    public String showSchema(){
        return df.schema().toString();
    }

    public ZFrame<Dataset<Row>,Row,Column> orderBy(String c){
        return new SparkFrame(df.orderBy(c));

    }

    public ZFrame<Dataset<Row>,Row,Column> limit(int l) {
        return new SparkFrame(df.limit(l));
    }

    public ZFrame<Dataset<Row>,Row,Column>  sortAscending(String c){
        return new SparkFrame(df.sort(functions.asc(c)));
    }
    public ZFrame<Dataset<Row>,Row,Column>  sortDescending(String c){
        return new SparkFrame(df.sort(functions.desc(c)));
    }

    @Override
    public String getAsString(Row r, String colName) {
        return r.getAs(colName);
    }

    @Override
    public double getAsDouble(Row r, String colName) {
        return r.getAs(colName);
    }
    @Override
    public int getAsInt(Row r, String colName) {
        return r.getAs(colName);
    }

    @Override
    public Row head() {
        return df.head();
    }

    @Override
    public Object getOnlyObjectFromRow(Row r){
        return r.get(0);
    }

    @Override
    public void show(boolean a){
        df.show(a);
    }

    @Override
    public boolean isEmpty() {
        return df.isEmpty();
    }
    
    @Override
    public ZFrame<Dataset<Row>, Row, Column> split(String colName,String pattern, String resultColName) {
    	return new SparkFrame(df.select(functions.split(df.col(colName), pattern).as(resultColName)));
    }
    
    @Override
    public ZFrame<Dataset<Row>, Row, Column> explode(String colName, String resultColName) {
    	return new SparkFrame(df.select(functions.explode(df.col(colName)).as(resultColName)));
    }
    
    @Override
    public String[] fieldNames() {
    	return df.schema().fieldNames();
    }    
    
}