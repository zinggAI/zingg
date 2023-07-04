package zingg.common.client;

import zingg.common.client.license.IZinggLicense;

public interface ZSession<S> {

	public S getSession();

	public void setSession(S session);

	public IZinggLicense getLicense();

	public void setLicense(IZinggLicense license);

	
}
