package eu.lod2.rsine.dissemination.notifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class LoggingNotifier implements INotifier {

    private final Logger logger = LoggerFactory.getLogger(LoggingNotifier.class);

    @Override
    public void notify(Collection<String> messages) {
        for (String message : messages) {
            logger.info(message);
        }
    }

}
