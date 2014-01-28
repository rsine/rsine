package at.punkt.lod2.util;

import java.util.concurrent.Callable;

public class ExpectedCountReached implements Callable<Boolean> {

    private CountingNotifier notifier;
    private int expectedCount;

    public ExpectedCountReached(CountingNotifier notifier, int expectedCount) {
        this.notifier = notifier;
        this.expectedCount = expectedCount;
    }

    @Override
    public Boolean call() throws Exception {
        return notifier.getNotificationCount() == expectedCount;
    }
}