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

package zingg.common.client;

import java.util.List;


//Dataset, Row, column
public interface ZFrame<D, R, C> {
	
	public static final String RIGHT_JOIN = "right";
	public static final String LEFT_JOIN = "left";
	
	public static final String COL_COUNT = "count";
	public static final String COL_VALUE = "VALUE";
	
	public static final String orSeperator = "\\|";	
	public static final String andSeperator = "\\&";	

	
    public ZFrame<D, R, C> cache();
    public ZFrame<D, R, C> as(String s);
    public String[] columns();
    public ZFrame<D, R, C> select(C... cols);
    public ZFrame<D, R, C> select(List<C> cols);
    public ZFrame<D, R, C> select(String col, String... cols);
    public ZFrame<D, R, C> select(String col);
    public ZFrame<D, R, C> selectExpr(String... col);
    public ZFrame <D, R, C> distinct();
    public List<R> collectAsList();
    public List<String> collectAsListOfStrings();

    public ZFrame<D, R, C> toDF(String[] cols);
    public ZFrame<D, R, C> toDF(String col1, String col2);

    public ZFrame<D, R, C> join(ZFrame<D, R, C> lines1, String joinColumn);

    /**doesnt dupe the join col */
    public ZFrame<D, R, C> joinOnCol(ZFrame<D, R, C> lines1, String joinColumn);
    
    public ZFrame<D, R, C> joinOnCol(ZFrame<D, R, C> lines1, C joinColumn);

    public ZFrame<D, R, C> join(ZFrame<D, R, C> lines1, String joinColumn1, String joinColumn2);
    
    public ZFrame<D, R, C> join(ZFrame<D, R, C> lines1, String joinColumn1, String joinColumn2, String jointype);

    public ZFrame<D, R, C> joinRight(ZFrame<D, R, C> lines1, String joinColumn);

    public ZFrame<D, R, C> join(ZFrame<D, R, C> lines1, String joinColumn, boolean addPrefixToCol, String jointype);    

    public C col(String colname);
    
    public long count();

    public ZFrame<D, R, C> filter(C col);

    public D df();

    public ZFrame<D, R, C> withColumnRenamed(String s, String t);

    public ZFrame<D, R, C> dropDuplicates(String c, String... d);

    public ZFrame<D, R, C> dropDuplicates(String[] c);

    public ZFrame<D, R, C> drop(String c);
    public ZFrame<D, R, C> drop(C c);
    public ZFrame<D, R, C> drop(String... c);
    public ZFrame<D, R, C> except(ZFrame<D, R, C> c);
    
    public double aggSum(String colName);

    public ZFrame<D, R, C> groupByMinMaxScore(C c);
    
    public ZFrame<D, R, C> groupByCount(String colName, String countColName);

    public ZFrame<D, R, C> union(ZFrame<D, R, C> other);
    
    public ZFrame<D, R, C> unionAll(ZFrame<D, R, C> other);

    public ZFrame<D, R, C> unionByName(ZFrame<D, R, C> other, boolean flag);

    public ZFrame<D, R, C> withColumn(String s, int c);
    public ZFrame<D, R, C> withColumn(String s, String c);
    public ZFrame<D, R, C> withColumn(String s, double c);
    public ZFrame<D, R, C> withColumn(String s, C c);

    
    public ZFrame<D, R, C> repartition(int num);
    public ZFrame<D, R, C> repartition(int num, C c);

    public ZFrame<D, R, C> sample(boolean repartition, float num);
    public ZFrame<D, R, C> sample(boolean repartition, double num);


    public ZFrame<D, R, C> coalesce(int num);

    public C gt(String c);
    
    public C gt(ZFrame<D, R, C> other, String c);    

    public C gt(String c, double val);
    
	public C equalTo(String c, String e);

	public C notEqual(String c, String e);
    
    public C equalTo(String c, int e);
    public C equalTo(String c, double e);
    public C concat(C a, C b);

	public C notEqual(String c, int e);


    public void show(int num);
    public void show();

    public String showSchema();

    public ZFrame<D, R, C> orderBy(String c);
    public ZFrame<D, R, C> sortAscending(String c);
    public ZFrame<D, R, C> sortDescending(String c);


    public ZFrame<D, R, C> limit(int l);

    public String getAsString(R r, String colName);

    public double getAsDouble(R r, String colName);

    public int getAsInt(R r, String colName);
    
    public long getAsLong(R r, String colName);

    public R head();

    public Object getOnlyObjectFromRow(R r);

    public void show(boolean a);

    public boolean isEmpty();
    
    public ZFrame<D, R, C> split(String colName,String pattern, String resultColName);
    
    /**
     * Creates a new row for each element in the given array or map column
     */
    public ZFrame<D, R, C> explode(String colName, String resultColName);
    
    public String[] fieldNames();
    

    public int fieldIndex(String colName);
    
    public FieldData[] fields();
    
    public Object getMaxVal(String colName);
    
	public ZFrame<D, R, C> filterInCond(String colName,ZFrame<D, R, C> innerDF, String innerDFCol);
    
	public ZFrame<D, R, C> filterNotNullCond(String colName);
	
	public ZFrame<D, R, C> filterNullCond(String colName);
	
	public C getObviousDupesFilter(String obviousDupeString, C extraAndCond);
    
	public C getObviousDupesFilter(ZFrame<D, R, C> dfToJoin, String obviousDupeString, C extraAndCond);
	
	public C getReverseObviousDupesFilter(String obviousDupeString, C extraAndCond);
	
	public C getReverseObviousDupesFilter(ZFrame<D, R, C> dfToJoin, String obviousDupeString, C extraAndCond);
	
}