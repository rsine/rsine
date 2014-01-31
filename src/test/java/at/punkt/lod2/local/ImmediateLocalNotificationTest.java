package at.punkt.lod2.local;

import at.punkt.lod2.util.CountingNotifier;
import at.punkt.lod2.util.ExpectedCountReached;
import com.jayway.awaitility.Awaitility;
import eu.lod2.rsine.dissemination.notifier.INotifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"LocalTest-context.xml"})
public class ImmediateLocalNotificationTest extends LocalNotificationTest {

    protected CountingNotifier countingNotifier = new CountingNotifier();

    @Test
    public void notificationDissemination() throws RDFParseException, IOException, RDFHandlerException {
        postEditChanges();
        Awaitility.await().atMost(20, TimeUnit.SECONDS).until(new ExpectedCountReached(countingNotifier, 1));
    }

    @Override
    protected INotifier getNotifier() {
        return countingNotifier;
    }
}
