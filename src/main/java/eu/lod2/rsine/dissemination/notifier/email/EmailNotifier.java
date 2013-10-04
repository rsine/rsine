package eu.lod2.rsine.dissemination.notifier.email;

import eu.lod2.rsine.Rsine;
import eu.lod2.rsine.dissemination.notifier.INotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

public class EmailNotifier implements INotifier {

    private final Logger logger = LoggerFactory.getLogger(EmailNotifier.class);

    private final String PROP_KEY_FROM = "emailnotifier.from";
    private final String PROP_KEY_HOST = "emailnotifier.host";
    private final String PROP_KEY_SMTP_HOST = "mail.smtp.host";
    
    private String emailAddress, from, smtpServer, subject = "[WP5] Notification";

    
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
            smtpServer = (String) properties.get("emailnotifier.smtpserver");
            from = (String) properties.get("emailnotifier.from");
            subject = (String) properties.get("emailnotifier.subject");
        }
        catch (IOException e) {
            logger.error("Could not read notifiers properties file");
        }
    }

    @Override
    public void notify(Collection<String> messages) {
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", "localhost");
        Session session = Session.getDefaultInstance(properties);

        try{         
           MimeMessage message = new MimeMessage(session);
           message.setFrom(new InternetAddress(from));
           message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailAddress));
           message.setSubject(subject);
           StringBuilder m = new StringBuilder();
           for(String s : messages){
               m.append(s);
               m.append("\n");
           }
           message.setText(m.toString());
           Transport.send(message);
            logger.info("Sent email to '" +emailAddress+ "'");
        }catch (Exception mex) {
            mex.printStackTrace();
           logger.warn(mex.getMessage());
        }      
    }

    public String getSmtpServer() {
        return smtpServer;
    }

}
