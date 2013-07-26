package eu.lod2.rsine.dissemination;

import eu.lod2.rsine.registrationservice.Subscription;
import org.openrdf.query.BindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Notifier {

    private final Logger logger = LoggerFactory.getLogger(Notifier.class);

    public void queryResultsAvailable(BindingSet bs, Subscription subscription) {
        logger.info("notifying subscriber: " + bs);
    }

}
