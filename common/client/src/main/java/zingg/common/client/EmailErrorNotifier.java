package zingg.common.client;

import zingg.common.client.util.Email;
import zingg.common.client.util.EmailBody;

public class EmailErrorNotifier implements IErrorNotifier {

    @Override
    public void notify(ClientOptions options, String subject, String message) {
        if (options != null && options.get(ClientOptions.EMAIL) != null) {
            Email.email(
                    options.get(ClientOptions.EMAIL).getValue(),
                    new EmailBody(subject, "Zingg Error : ", message)
            );
        }
    }
}
