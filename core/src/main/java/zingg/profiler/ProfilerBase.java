package zingg.profiler;

import java.io.File;

import org.apache.spark.sql.SparkSession;

import zingg.client.Arguments;

class ProfilerBase {
	protected SparkSession spark;
	protected Arguments args;

	public ProfilerBase(SparkSession spark, Arguments args) {
		this.spark = spark;
		this.args = args;
	}

	protected void checkAndCreateDir(String dirName) {
		File directory = new File(dirName);
		if (!directory.exists()) {
			directory.mkdirs();
		}
	}

}
