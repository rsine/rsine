package at.punkt.lod2;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"LocalTest-context.xml"})
public class ImmediateLocalNotificationTest extends LocalNotificationTest {

    @Test
    public void notificationDissemination() throws RDFParseException, IOException, RDFHandlerException {
        registerUser();
        postEditChanges();
        countingNotifier.waitForNotification();
    }

}
