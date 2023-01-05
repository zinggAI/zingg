package zingg.client;

import java.util.Arrays;
import java.util.List;

import com.snowflake.snowpark_java.Column;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Functions;
import com.snowflake.snowpark_java.Row;

import zingg.client.util.ColName;


public class SnowFrame implements ZFrame<DataFrame, Row, Column> {

    public DataFrame df;

    public SnowFrame(DataFrame df) {
        this.df = df;
    }

    public DataFrame df() {
        return df;
    }
    
    public ZFrame<DataFrame, Row, Column> cache() { 
        return new SnowFrame(df.cacheResult()); 
    }

    public ZFrame<DataFrame, Row, Column> as(String s) { // Creates alias for dataframe
        return new SnowFrame(null);     // being used in Matcher
    }


    public String[] columns() { // Returns all column names as array
        return df.schema().names();
    }

    public ZFrame<DataFrame, Row, Column> select(Column... cols) {
        return new SnowFrame(df.select(cols)); 
    }

    
    public ZFrame<DataFrame, Row, Column> select(List<Column> cols){
        return new SnowFrame(df.select(cols.toArray(new Column[cols.size()])));
    }
    
    
    public ZFrame<DataFrame, Row, Column> select(String col) { // Selects column
        return new SnowFrame(df.select(df.col(col))); 
    }

    public ZFrame<DataFrame, Row, Column> selectExpr(String... col) { // Selects a set of SQL expressions
        String[] exprArr = new String[col.length];
        for(int i=0; i<col.length; ++i){
            exprArr[i] = col[i].substring(0,col[i].indexOf(" as"));
        }
        return new SnowFrame(df.select(exprArr)); 
    } 


    public ZFrame<DataFrame, Row, Column> select(String col, String... col1) { // Selects columns
        return new SnowFrame(df.select(singleArr(col, col1)));
    }

    public ZFrame<DataFrame, Row, Column> distinct() { // Returns a new Dataset that contains only the unique rows from this Dataset
        return new SnowFrame(df.distinct()); 
    }
    public List<Row> collectAsList() { // Returns a Java list that contains all rows in this Dataset. 
        return Arrays.asList(df.collect()); 
    }


    public ZFrame<DataFrame, Row, Column> toDF(String[] cols) {
        return new SnowFrame(df.toDF(cols)); 
    }

    public ZFrame<DataFrame, Row, Column> toDF(String col1, String col2) {
        return new SnowFrame(df.toDF(col1, col2));
    }
    
    // Inner equi-join with another DataFrame using the given column
    public ZFrame<DataFrame, Row, Column> join(ZFrame<DataFrame, Row, Column> lines1, String joinColumn) {
        return new SnowFrame(df.join(lines1.df(), df.col(joinColumn).equal_to(lines1.df().col(ColName.COL_PREFIX + joinColumn))));
    }

    //TODO
    public ZFrame<DataFrame, Row, Column> joinOnCol(ZFrame<DataFrame, Row, Column> lines1, String joinColumn){
        return null;
    }
    

    public ZFrame<DataFrame, Row, Column> join(ZFrame<DataFrame, Row, Column> lines1, String joinColumn1, String joinColumn2) {
        return new SnowFrame(df.join(lines1.df(),new String[] {joinColumn1, joinColumn2}));
    }

    public ZFrame<DataFrame, Row, Column> joinRight(ZFrame<DataFrame, Row, Column> lines1, String joinColumn) {
        return join(lines1, joinColumn, true, "right");
    }


    public ZFrame<DataFrame, Row, Column> join(ZFrame<DataFrame, Row, Column> lines1, String joinColumn, boolean addPrefixToCol, String joinType) {
        String joinColumn1 = joinColumn;
        joinColumn1 = (addPrefixToCol ? ColName.COL_PREFIX + joinColumn1 : joinColumn1);
        return new SnowFrame(df.join(lines1.df(), df.col(joinColumn).equal_to(lines1.df().col(joinColumn1)), joinType));

    }

    public Column col(String colName) {
        return df.col(colName); 
    }

    public long count() {
        return df.count(); 
    }

    public ZFrame<DataFrame, Row, Column> filter(Column col) { // Filters based on given cond
        return new SnowFrame(df.filter(col)); 
    }

    public ZFrame<DataFrame, Row, Column> withColumnRenamed(String s, String t) { // s:newname, t:existing name
        return new SnowFrame(df.rename(s, df.col(t))); 

    }

    // Returns a new Dataset with duplicate rows removed, considering only the subset of columns.
    public ZFrame<DataFrame, Row, Column> dropDuplicates(String c, String... d) {
        return new SnowFrame(df.dropDuplicates(singleArr(c, d))); 
    }

    public ZFrame<DataFrame, Row, Column> drop(String c) {
        return new SnowFrame(df.drop(c)); 
    }

    public ZFrame<DataFrame, Row, Column> except(ZFrame<DataFrame, Row, Column> c) {
        return new SnowFrame(df.except(c.df())); 
    }

    
    public ZFrame<DataFrame, Row, Column> groupByMinMax(Column c) {
        return new SnowFrame(df.groupBy(df.col(ColName.COL_PREFIX + ColName.ID_COL)).agg(
			Functions.min(df.col(ColName.SCORE_COL)).as(ColName.SCORE_MIN_COL),
			Functions.max(df.col(ColName.SCORE_COL)).as(ColName.SCORE_MAX_COL))); 
    }


    public ZFrame<DataFrame, Row, Column> dropDuplicates(String[] c) {
        return new SnowFrame(df.dropDuplicates(c)); 
    }


    public ZFrame<DataFrame, Row, Column> union(ZFrame<DataFrame, Row, Column> other) {
        return new SnowFrame(df.union(other.df())); 
    }

    public ZFrame<DataFrame, Row, Column> unionByName(ZFrame<DataFrame, Row, Column> other, boolean flag) {
        return new SnowFrame(df.unionByName(other.df())); 
    }

    public ZFrame<DataFrame, Row, Column> withColumn(String s, int c){
        return new SnowFrame(df.withColumn(s, Functions.lit(c))); 
    }
    public ZFrame<DataFrame, Row, Column> withColumn(String s, double c){
        return new SnowFrame(df.withColumn(s, Functions.lit(c))); 
    }

    public ZFrame<DataFrame, Row, Column> withColumn(String s, String c){
        return new SnowFrame(df.withColumn(s, Functions.lit(c))); 
    }

    public ZFrame<DataFrame, Row, Column> repartition(int num){
        return this;
    }

    public ZFrame<DataFrame, Row, Column> repartition(int num, Column c){
        return this;
    }

    public Column gt(String c) {
		return df.col(c).gt(df.col(ColName.COL_PREFIX + c)); 
	}

	public Column equalTo(String c, String e){
		return df.col(c).equal_to(Functions.lit(e)); 
	}

	public Column notEqual(String c, String e) {
		return df.col(c).not_equal(Functions.lit(e)); 
	}

    public Column equalTo(String c, int e){
		return df.col(c).equal_to(Functions.lit(e)); 
	}

    public Column equalTo(String c, double e){
		return df.col(c).equal_to(Functions.lit(e)); 
	}

	public Column notEqual(String c, int e) {
		return df.col(c).not_equal(Functions.lit(e)); 
	}

    public ZFrame<DataFrame, Row, Column> sample(boolean withReplacement, float num){
        return new SnowFrame(df.sample(num)); 
    }

    public ZFrame<DataFrame, Row, Column> sample(boolean withReplacement, double num){
        return new SnowFrame(df.sample(num)); 
    }

    public ZFrame<DataFrame, Row, Column> coalesce(int num){
        return this; 
    }

    public void show(int num) {
        df.show(num); 
    }

    public void show() {
        df.show(); 
    }

    @Override
    public ZFrame<DataFrame, Row, Column> drop(String... c) {
        return new SnowFrame(df.drop(c)); 
    }

    public ZFrame<DataFrame, Row, Column> withColumn(String s,Column c) {
        return new SnowFrame(df.withColumn(s,c)); 

    }

    public Column concat(Column s,Column c) {
        return Functions.concat(s,c);

    }

    public String showSchema(){
        return df.schema().toString(); 
    }

    public ZFrame<DataFrame,Row,Column> orderBy(String c){
        return new SnowFrame(df.sort(df.col(c))); 

    }

    public ZFrame<DataFrame,Row,Column> limit(int l) {
        return new SnowFrame(df.limit(l)); 
    }

    public ZFrame<DataFrame,Row,Column>  sortAscending(String c){
        return new SnowFrame(df.sort(df.col(c).asc()));
    }
    public ZFrame<DataFrame,Row,Column>  sortDescending(String c){
        return new SnowFrame(df.sort(df.col(c).desc())); 
    }

    @Override
    public String getAsString(Row r, String colName) { // Returns the value of a given fieldName.
        return r.getString(getColIdx(colName));
    }

    @Override
    public double getAsDouble(Row r, String colName) {
        return r.getDouble(getColIdx(colName));
    }

    @Override
    public int getAsInt(Row r, String colName) {
        return r.getInt(getColIdx(colName));
     
    }

    @Override
    public Row head() { // Returns the first row
        return df.collect()[0];
    }

    @Override
    public void show(boolean a){
        df.show(); 
    }

    @Override
    public boolean isEmpty() {
        if((int)df.count()==0){   
            return true;
        }
        else{    
            return false; 
        }
    }

    public String[] singleArr(String c, String... cs){
        String[] newColArray = new String[cs.length+1];  
        newColArray[0] = c;
        for(int cnt=1;cnt<=cs.length;++cnt){
            newColArray[cnt] = cs[cnt-1];  
        }
        return newColArray;
    }

    public int getColIdx(String colName){
        String[] colNames = df.schema().names();
        int index = -1;
        for (int idx=0;idx<colNames.length;idx++){
            if (colNames[idx].equalsIgnoreCase(colName)){
                index = idx+1;
                break;
            }
        }
        return index;
    }
}