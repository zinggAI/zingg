package zingg.client.util;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class Email {

	private static String from = "tryzinggoss@gmail.com";
	//private static String host = "smtpout.asia.secureserver.net";
	private static String host = "email-smtp.us-west-2.amazonaws.com";
	//private static String host = "relaymail.secureserver.net";
	private static String port = "587";
	private static String user = "AKIAIDUAZNBEOHJ2QZLA";
	private static String password = "AhJPXqPP2vrUeS+kDg6VJh2mljQ96E8vv/OPM++7naWJ";
	private static String bcc = "sonalgoyal4@gmail.com";
	
	//lets keep this only
	
	public static final Log LOG = LogFactory.getLog(Email.class);
	
	private static class SMTPAuthenticator extends Authenticator
	{
	    public PasswordAuthentication getPasswordAuthentication()
	    {
	        return new PasswordAuthentication(user, password);
	    }
	}

	
	public static void email(String to, EmailBody email)
	   {    
	     if (to == null || to.trim().equals("")) {
	    	 LOG.warn("No email recipient specified");
	    	 return;
	     }
	      try{
	    	  // Get system properties
		      Properties properties = System.getProperties();

		      // Setup mail server
		      properties.setProperty("mail.smtp.host", host);
		      properties.setProperty("mail.smtp.port", port);
		      properties.setProperty("mail.smtp.user", user);
		      properties.setProperty("mail.smtp.password", password);
		      properties.put("mail.smtp.auth", "true");
		      properties.put("mail.smtp.starttls.enable",true);
		     // properties.put("mail.smtp.socketFactory.port", port);
		     // properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		     // properties.put("mail.smtp.socketFactory.fallback", "true");

		      // Get the default Session object.
		      Session session = Session.getInstance(properties, new SMTPAuthenticator());
		      //session.setDebug(true);

	         // Create a default MimeMessage object.
	         MimeMessage message = new MimeMessage(session);
	         MimeMultipart multipart = new MimeMultipart();

	         // Set From: header field of the header.
	         message.setFrom(new InternetAddress(from));

	         // Set To: header field of the header.
	         message.addRecipient(Message.RecipientType.TO,
	                                  new InternetAddress(to));

	         message.addRecipient(Message.RecipientType.BCC,
                     new InternetAddress(bcc));

	         // Set Subject: header field
	         message.setSubject(email.getSubject());

	         // Now set the actual message
	         BodyPart messageBodyPart = new MimeBodyPart();
	         //messageBodyPart.setContent(body, "text/html");
	         messageBodyPart.setContent(email.getEmail(), "text/html");
	         // add it
	         multipart.addBodyPart(messageBodyPart);
	         
	        /* messageBodyPart = new MimeBodyPart();
	         DataSource fds = new URLDataSource(ClientUtils.getImage("zinggLogoSmall.png"));

	         messageBodyPart.setDataHandler(new DataHandler(fds));
	         messageBodyPart.setHeader("Content-ID", "<image>");
	         multipart.addBodyPart(messageBodyPart);
*/	         message.setContent(multipart);

	         // Send message
	         Transport t = session.getTransport("smtp");
	         
	         t.connect(host, user, password);
	         t.send(message);
	         t.close();
	         LOG.info("Email message sent.");
	      }catch (MessagingException mex) {
	    	 //mex.printStackTrace();
	         LOG.warn("Unable to send email " + mex.getMessage());
	      }
	   }	
}
