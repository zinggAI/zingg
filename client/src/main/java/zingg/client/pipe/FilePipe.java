package zingg.client.pipe;


import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.types.StructType;

import java.util.Map;

public class FilePipe extends Pipe{
	
	public static final String LOCATION = "location";


	private FilePipe(
			String name,
			Format format,
			String preprocessors,
			StructType schema,
			Map<String, String> props,
			Map<String, String> sparkProps,
			Map<String, String> addProps,
			SaveMode mode,
			int id) {
		super(
				name,
				format,
				preprocessors,
				schema,
				props,
				sparkProps,
				addProps,
				mode,
				id
		);
	}

	public static FilePipe fromPipe(Pipe p){
		return null;
	}

}
