package eu.lod2.rsine.dissemination.notifier.email;

import eu.lod2.rsine.dissemination.notifier.INotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class EmailNotifier implements INotifier {

    private final Logger logger = LoggerFactory.getLogger(EmailNotifier.class);
    private String emailAddress;

    public EmailNotifier(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @Override
    public void notify(Collection<String> messages) {
        logger.info("sending email to '" +emailAddress+ "'");

        //implement me: actually send the email
    }

}
