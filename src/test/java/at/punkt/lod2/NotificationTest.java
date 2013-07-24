package at.punkt.lod2;

import eu.lod2.rsine.Rsine;
import eu.lod2.rsine.registrationservice.Subscription;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;

import java.io.IOException;

public class NotificationTest {

    private Rsine rsine;

    @Before
    public void setUp() throws IOException, RepositoryException {
        rsine = new Rsine();
    }

    @Test
    public void notificationDissemination() {
        registerUser();
        postTripleChanges();

    }

    private void registerUser() {
        Subscription subscription = rsine.requestSubscription();
        subscription.addQuery(createQuery());
        rsine.registerSubscription(subscription);
    }

    private String createQuery() {
        return "testQuery1";
    }

    private void postTripleChanges() {
        createConcept();

        /* TODO: more changes to cover typical usage scenarios
        addPrefLabel();
        changePrefLabel();
        createOtherConcept();
        linkConcepts();
        */
    }

    private void createConcept() {

    }

}
