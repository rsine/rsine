package eu.lod2.rsine.dissemination.notifier.email;

import eu.lod2.rsine.dissemination.notifier.INotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Collection;
import java.util.Properties;

public class EmailNotifier implements INotifier {

    private final Logger logger = LoggerFactory.getLogger(EmailNotifier.class);
    private final String PROP_KEY_FROM = "emailnotifier.from";
    private final String PROP_KEY_HOST = "emailnotifier.host";
    private final String PROP_KEY_SMTP_HOST = "mail.smtp.host";
    
    private String emailAddress;
    private Properties properties;
    private String from;
    private String host;
    
    public EmailNotifier(String emailAddress) {
        this.emailAddress = emailAddress.replaceFirst("mailto:", "");
        properties = System.getProperties();
        from = properties.containsKey(PROP_KEY_FROM)?properties.getProperty(PROP_KEY_FROM):"wp5emailnotifier@lod2.eu";
        if(!properties.containsKey(PROP_KEY_SMTP_HOST)){
            if(properties.containsKey(PROP_KEY_HOST)){
                properties.setProperty(PROP_KEY_SMTP_HOST, properties.getProperty(PROP_KEY_HOST));
            } else {
                properties.setProperty(PROP_KEY_SMTP_HOST, "localhost");
            }
        }
    }

    @Override
    public void notify(Collection<String> messages) {
        logger.info("sending email to '" +emailAddress+ "'");
        Session session = Session.getDefaultInstance(properties);
        try{         
           MimeMessage message = new MimeMessage(session);
           message.setFrom(new InternetAddress(from));
           message.addRecipient(Message.RecipientType.TO, new InternetAddress(this.emailAddress));
           message.setSubject("WP5Notification");
           StringBuilder m = new StringBuilder();
           for(String s : messages){
               m.append(s);
               m.append("\n");
           }
           message.setText(m.toString());
           Transport.send(message);         
        }catch (Exception mex) {
            mex.printStackTrace();
           logger.warn(mex.getMessage());
        }      
    }

}
