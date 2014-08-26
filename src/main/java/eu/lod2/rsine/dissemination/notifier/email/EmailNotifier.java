package eu.lod2.rsine.dissemination.notifier.email;

import eu.lod2.rsine.Rsine;
import eu.lod2.rsine.dissemination.notifier.INotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

public class EmailNotifier implements INotifier {

    private final Logger logger = LoggerFactory.getLogger(EmailNotifier.class);

    private String emailAddress, from, host, port, subject = "[WP5] Notification", username, password;
    
    public EmailNotifier(String emailAddress) {
        this.emailAddress = emailAddress.replaceFirst("mailto:", "");
        readSettingsFromProperties();
    }

    private void readSettingsFromProperties() {
        Properties properties = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream(Rsine.propertiesFileName);

        try {
            properties.load(stream);
            host = (String) properties.get("emailnotifier.smtp.host");
            port = (String) properties.get("emailnotifier.smtp.port");
            from = (String) properties.get("emailnotifier.from");
            subject = (String) properties.get("emailnotifier.subject");
            username = (String) properties.get("emailnotifier.smtp.username");
            password = (String) properties.get("emailnotifier.smtp.password");
        }
        catch (IOException e) {
            logger.error("Could not read notifiers properties file");
        }
    }

    @Override
    public void notify(Collection<String> messages) {
        try {
            MimeMessage message = new MimeMessage(createSession());
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailAddress));

            message.setFrom(new InternetAddress(from));
            message.setSubject(subject);
            message.setContent(createMessageText(messages).toString(), "text/html; charset=utf-8");

            logger.info("Sending email notification");
            Transport.send(message);
        }
        catch (Exception e) {
            logger.error("Could not send email to '" +emailAddress+ "': " +e.getMessage());
        }
    }

    private Session createSession() {
        return Session.getInstance(
            createMailProperties(),
            new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
    }

    private Properties createMailProperties() {
        Properties properties = new Properties();

        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", true);
        properties.put("mail.smtp.starttls.enable", true);

        return properties;
    }

    private StringBuilder createMessageText(Collection<String> messages) throws MessagingException {
        StringBuilder m = new StringBuilder();
        for(String s : messages){
            m.append(s);
            m.append("\n");
        }
        return m;
    }

}
