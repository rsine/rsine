package at.punkt.lod2.local;

import at.punkt.lod2.util.CountingNotifier;
import eu.lod2.rsine.dissemination.notifier.INotifier;
import org.junit.Assert;
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

    protected CountingNotifier countingNotifier = new CountingNotifier();

    @Test
    public void notificationDissemination() throws RDFParseException, IOException, RDFHandlerException {
        postEditChanges();
        Assert.assertEquals(1, countingNotifier.getNotificationCount());
    }

    @Override
    protected INotifier getNotifier() {
        return countingNotifier;
    }
}
