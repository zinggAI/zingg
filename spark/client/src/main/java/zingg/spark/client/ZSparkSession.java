package zingg.spark.client;
import org.apache.spark.sql.SparkSession;

import zingg.common.client.ZSession;
import zingg.common.client.license.IZinggLicense;

public class ZSparkSession implements ZSession<SparkSession> {
	
	private SparkSession session;
	
	private IZinggLicense license;

	public ZSparkSession(SparkSession session, IZinggLicense license) {
		super();
		this.session = session;
		this.license = license;
	}	
	
	@Override
	public SparkSession getSession() {
		return session;
	}

	@Override
	public void setSession(SparkSession session) {
		this.session = session;
	}

	@Override
	public IZinggLicense getLicense() {
		return license;
	}

	@Override
	public void setLicense(IZinggLicense license) {
		this.license = license;
	}

}
