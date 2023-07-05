package zingg.common.client.license;

import java.util.Properties;

public interface IZinggLicense {
	
	public ILicenseValidator getValidator(String name);
	
	public Properties getLicenseProps();
	
}
