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

package zingg.spark.client.pipe;

import java.io.IOException;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructType;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import org.apache.spark.sql.Dataset;
import zingg.common.client.pipe.Pipe;
import zingg.spark.client.SparkFrame;

public class SparkPipe extends Pipe<Dataset<Row>, Row, Column> {
    
    @JsonSerialize(using = CustomSchemaSerializer.class)
	protected StructType schemaStruct;

    public void setDataset(Dataset<Row> ds){
		this.dataset = new SparkFrame(ds);
	}
    

    static class CustomSchemaSerializer extends StdSerializer<StructType> {
		 
        public CustomSchemaSerializer() { 
           this(null); 
       } 
    
       public CustomSchemaSerializer(Class<StructType> t) {
           super(t); 
       }
    
       @Override
       public void serialize(
               StructType value, JsonGenerator gen, SerializerProvider arg2) 
         throws IOException, JsonProcessingException {
           gen.writeObject(value.json());
       }
   }

    
	public StructType getSchemaStruct() {
		return schemaStruct;
	}
	
}
