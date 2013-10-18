package at.punkt.lod2;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"LocalTestMinTimePassed-context.xml"})
public class MinTimePassedLocalNotificationTest extends LocalNotificationTest {

    @Test
    public void notificationDissemination() throws RDFParseException, IOException, RDFHandlerException {
        registerUser();
        changePrefLabel();

        //immediate notification (1st time of query)

        changePrefLabel();
        //no notification (change too soon)

        try {
            Thread.sleep(7000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        changePrefLabel();
        //now we get notified again

        countingNotifier.waitForNotification();

        Assert.fail();
    }

}
