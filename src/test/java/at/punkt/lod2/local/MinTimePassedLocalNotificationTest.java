package at.punkt.lod2.local;

import com.jayway.awaitility.Awaitility;
import eu.lod2.rsine.dissemination.notifier.INotifier;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"LocalTestMinTimePassed-context.xml"})
public class MinTimePassedLocalNotificationTest extends LocalNotificationTest {

    private final long IMMEDIATE_NOTIFICATION_THRESHOLD_MILLIS = 1000;
    private TimeMeasureNotifier timeMeasureNotifier = new TimeMeasureNotifier();

    @Test
    public void immediateNotificationOnFirstChange() throws IOException {
        performChange();

        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(new NotificationDetector(timeMeasureNotifier));
        Assert.assertTrue(timeMeasureNotifier.millisPassed < IMMEDIATE_NOTIFICATION_THRESHOLD_MILLIS);
    }

    private void performChange() throws IOException {
        timeMeasureNotifier.reset();
        changePrefLabel();
    }

    @Test
    public void changeTooSoonForNotification() throws IOException {
        performChange();
        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(new NotificationDetector(timeMeasureNotifier));

        performChange();
        Assert.assertFalse(notificationReceivedWithinASecond());
    }

    private boolean notificationReceivedWithinASecond() {
        long start = System.currentTimeMillis();
        while (timeMeasureNotifier.millisPassed == null) {
            if (System.currentTimeMillis() - start > 1000) return false;
        }
        return true;
    }

    @Test
    public void changeLateEnoughForNotification() throws IOException {
        performChange();
        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(new NotificationDetector(timeMeasureNotifier));

        try {
            Thread.sleep(15000);
        }
        catch (InterruptedException e) {
            // ignore
        }

        changePrefLabel();
        Assert.assertTrue(notificationReceivedWithinASecond());
    }

    @Test
    public void lastChangeNotMissed() throws IOException {
        performChange();
        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(new NotificationDetector(timeMeasureNotifier));

        performChange();
        Awaitility.await().atMost(20, TimeUnit.SECONDS).until(new NotificationDetector(timeMeasureNotifier));
    }

    @Override
    protected INotifier getNotifier() {
        return timeMeasureNotifier;
    }

    private class NotificationDetector implements Callable<Boolean> {

        private TimeMeasureNotifier notifier;

        private NotificationDetector(TimeMeasureNotifier notifier) {
            this.notifier = notifier;
        }

        @Override
        public Boolean call() throws Exception {
            return notifier.getMillisPassed() != null;
        }
    }

    private class TimeMeasureNotifier implements INotifier {

        private long time = 0;
        private Long millisPassed = null;

        private void reset() {
            millisPassed = null;
            time = System.currentTimeMillis();
        }

        @Override
        public void notify(Collection<String> messages) {
            millisPassed = System.currentTimeMillis() - time;
        }

        public Long getMillisPassed() {
            return millisPassed;
        }

    }

}
