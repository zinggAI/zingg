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

import org.apache.spark.sql.Column;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;

import zingg.common.client.ZFrame;
import zingg.common.core.util.DFWriter;

public class SparkDFWriter implements DFWriter<Dataset<Row>, Row, Column>{
    private DataFrameWriter writer;

    public SparkDFWriter(ZFrame<Dataset<Row>, Row, Column> toWriteOrig) {
        Dataset<Row> toWrite = toWriteOrig.df();
		this.writer = toWrite.write();
        
    }


    public void setMode(String s) {
        this.writer.mode(SaveMode.valueOf(s));

    }
    public DFWriter<Dataset<Row>, Row, Column> format(String f) {
        writer.format(f);
        return this;
    }
    public DFWriter<Dataset<Row>, Row, Column> option(String k, String v) {
        writer.option(k,v);
        return this;
    }
    public void save(String location) {
        writer.save(location);
    }
    public void save() {
        writer.save();
    }
    
}
