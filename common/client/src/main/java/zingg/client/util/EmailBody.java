package zingg.client.util;

import java.text.MessageFormat;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class EmailBody {
	private static ResourceBundle emailBundle = ResourceBundle.getBundle("email");
	private boolean isError;
	private String subject;
	private String header;
	private String body;
	private String template;
	
	private EmailBody() {
		this.template = emailBundle.getString("template");
	}
	
	public EmailBody(String subject, String header, String body) {
		this();
		this.subject = subject;
		this.header = header;
		this.body = body;
	}
	
	public boolean isError() {
		return isError;
	}
	public void setError(boolean isError) {
		this.isError = isError;
	}
	public String getHeader() {
		return header;
	}
	public void setHeader(String header) {
		this.header = header;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	
	public String getEmail() {
		return MessageFormat.format(template, header, body);
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	
	
			
			

}
