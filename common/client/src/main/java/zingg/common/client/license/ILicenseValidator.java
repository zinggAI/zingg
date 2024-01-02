package zingg.common.client.license;

import java.util.Properties;

public interface ILicenseValidator {

	public boolean validate();

	public Properties getLicenseProps();

	public void setLicenseProps(Properties licenseProps);

	public String getKey();

	public void setKey(String key);

	public String getValToCheck();

	public void setValToCheck(String valToCheck);

	public String getName();

	public void setName(String name);
	
}
