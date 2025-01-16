package zingg.spark.client.pipe;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;

import org.apache.spark.sql.Dataset;

import zingg.common.client.pipe.FilePipe;
import zingg.common.client.pipe.Pipe;


public class SparkPipe extends Pipe<Dataset<Row>, Row, Column> {

    public SparkPipe(){

    }
    
    /* 
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
    */

    public void setOverwriteMode() {
		setMode(SaveMode.Overwrite.toString());
    }

    public String massageLocation(String name){
        name = name.replaceAll("-", "");
        name = name.replaceAll("@","");
        name = name.replaceAll(",","");
        name = name.replaceAll(":","");
        name = name.replaceAll(".","");
        return name;
    }

    public void setLocation(String fileName){
        setProp(FilePipe.LOCATION, massageLocation(fileName));
    }

	
}
