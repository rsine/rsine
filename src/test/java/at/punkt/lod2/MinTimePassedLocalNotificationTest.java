package at.punkt.lod2;

import at.punkt.lod2.util.SuccessReportingPersistAndNotifyProvider;
import eu.lod2.rsine.dissemination.notifier.INotifier;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Collection;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"LocalTestMinTimePassed-context.xml"})
public class MinTimePassedLocalNotificationTest extends LocalNotificationTest {

    private final long IMMEDIATE_NOTIFICATION_THRESHOLD_MILLIS = 1000;
    private TimeMeasureNotifier timeMeasureNotifier = new TimeMeasureNotifier();
    private SuccessReportingPersistAndNotifyProvider successReportingPersistAndNotifyProvider;

    @Override
    public void setUp() throws IOException, RepositoryException, RDFParseException {
        super.setUp();
        successReportingPersistAndNotifyProvider = applicationContext.getBean(SuccessReportingPersistAndNotifyProvider.class);
    }

    @Test
    public void immediateNotificationOnFirstChange() throws IOException {
        performChange();
        timeMeasureNotifier.waitForNotification();

        Assert.assertTrue(timeMeasureNotifier.millisPassed < IMMEDIATE_NOTIFICATION_THRESHOLD_MILLIS);
        Assert.assertTrue(successReportingPersistAndNotifyProvider.getSuccess());
    }

    private void performChange() throws IOException {
        timeMeasureNotifier.reset();
        changePrefLabel();
    }

    @Test
    public void changeTooSoonForNotification() throws IOException {
        performChange();
        timeMeasureNotifier.waitForNotification();

        performChange();
        Assert.assertFalse(successReportingPersistAndNotifyProvider.getSuccess());
    }

    @Test
    public void changeLateEnoughForNotification() throws IOException {
        performChange();
        timeMeasureNotifier.waitForNotification();

        try {
            Thread.sleep(6000);
        }
        catch (InterruptedException e) {
            // ignore
        }

        performChange();
        timeMeasureNotifier.waitForNotification();
        Assert.assertTrue(successReportingPersistAndNotifyProvider.getSuccess());
    }

    @Override
    protected INotifier getNotifier() {
        return timeMeasureNotifier;
    }

    private class TimeMeasureNotifier implements INotifier {

        private long time = 0;
        private Long millisPassed = null;

        void reset() {
            millisPassed = null;
            time = System.currentTimeMillis();
        }

        @Override
        public void notify(Collection<String> messages) {
            millisPassed = System.currentTimeMillis() - time;
        }

        void waitForNotification() {
            while (millisPassed == null) {
                try {
                    Thread.sleep(200);
                }
                catch (InterruptedException e) {
                }
                Thread.yield();
            }
        }

    }

}
