package zingg.common.client.main;

import zingg.common.client.util.Email;
import zingg.common.client.util.EmailBody;

public class NotificationService {
    public void sendErrorEmail(String emailAddress, String subject, String message) {
        if (emailAddress != null) {
            Email.email(emailAddress, new EmailBody(subject, "Zingg Error", message));
        }
    }
}
