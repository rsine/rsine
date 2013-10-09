package at.punkt.lod2;

import at.punkt.lod2.util.Helper;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.rsine.registrationservice.SubscriptionParser;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import java.io.IOException;

public class SubscriptionParserTest {

    @Test
    public void emailNotificationSubscription() throws RDFParseException, IOException, RDFHandlerException {
        Model rdfSubscription = new Helper().createModelFromResourceFile("/emailNotifierSubscription.ttl", RDFFormat.TURTLE);
        Subscription subscription = new SubscriptionParser(rdfSubscription).createSubscription();

        Assert.assertTrue(subscription.getNotifierIterator().hasNext());
        Assert.assertTrue(subscription.getQueryIterator().hasNext());
    }

}
