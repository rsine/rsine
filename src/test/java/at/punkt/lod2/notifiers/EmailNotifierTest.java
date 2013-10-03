package at.punkt.lod2.notifiers;

import eu.lod2.rsine.dissemination.notifier.email.EmailNotifier;
import org.junit.Assert;
import org.junit.Test;

public class EmailNotifierTest {

    @Test
    public void loadProperties() {
        String smtpServer = new EmailNotifier("test@example.org").getSmtpServer();
        Assert.assertNotNull(smtpServer);
    }

}
