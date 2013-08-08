package at.punkt.lod2;

import eu.lod2.rsine.Rsine;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.rsine.registrationservice.SubscriptionParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.rio.*;
import org.openrdf.rio.helpers.StatementCollector;

import java.io.IOException;

public class SubscriptionParserTest {

    private Model rdfSubscription;
    private RDFParser rdfParser;

    @Before
    public void setUp() {
        rdfParser = Rio.createParser(RDFFormat.TURTLE);
        rdfSubscription = new TreeModel();
        StatementCollector collector = new StatementCollector(rdfSubscription);
        rdfParser.setRDFHandler(collector);
    }

    @Test
    public void emailNotificationSubscription() throws RDFParseException, IOException, RDFHandlerException {
        rdfParser.parse(Rsine.class.getResourceAsStream("/emailNotifierSubscription.ttl"), "");
        Subscription subscription = new SubscriptionParser(rdfSubscription).createSubscription();

        Assert.assertTrue(subscription.getNotifierIterator().hasNext());
        Assert.assertTrue(subscription.getQueryIterator().hasNext());
    }

}
