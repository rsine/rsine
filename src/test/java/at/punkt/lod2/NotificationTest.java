package at.punkt.lod2;

import eu.lod2.rsine.Rsine;
import eu.lod2.rsine.changesetservice.ChangeTripleHandler;
import eu.lod2.rsine.registrationservice.Subscription;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

public class NotificationTest {

    private Rsine rsine;

    @Before
    public void setUp() throws IOException, RepositoryException, RDFParseException {
        rsine = new Rsine();
        addVocabData();
    }

    private void addVocabData() throws RepositoryException, RDFParseException, IOException {
        URL vocabUrl = Rsine.class.getResource("/reegle.rdf");
        rsine.setManagedTripleStoreContent(new File(vocabUrl.getFile()));
    }

    @Test
    public void notificationDissemination() throws RDFParseException, IOException, RDFHandlerException {
        registerUser();
        postChanges();
    }

    private void registerUser() {
        Subscription subscription = rsine.requestSubscription();
        subscription.addQuery(createQuery());
        rsine.registerSubscription(subscription);
    }

    private String createQuery() {
        //TODO: preflabel changes of concepts created by the subscriber
        return "testQuery1";
    }

    private void postChanges() throws IOException {
        addConcept();
        setPrefLabel();
        changePrefLabel();
        addOtherConcept();
        linkConcepts();
    }

    private void addConcept() throws IOException {
        post(new BasicNameValuePair(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD),
             new BasicNameValuePair(
                ChangeTripleHandler.POST_BODY_TRIPLE,
                "<http://reegle.info/glossary/1111> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2004/02/skos/core#Concept> ."));
    }

    private void post(NameValuePair... nameValuePairs) throws IOException {
        HttpPost httpPost = new HttpPost("http://localhost:8080");

        httpPost.setEntity(new UrlEncodedFormEntity(Arrays.asList(nameValuePairs)));

        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.execute(httpPost);
    }

    private void setPrefLabel() throws IOException {
        post(new BasicNameValuePair(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD),
            new BasicNameValuePair(
                ChangeTripleHandler.POST_BODY_TRIPLE,
                "<http://reegle.info/glossary/1111> <http://www.w3.org/2004/02/skos/core#prefLabel> \"Ottakringer Helles\"@en ."));
    }

    private void changePrefLabel() throws IOException {
        post(new BasicNameValuePair(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_REMOVE),
            new BasicNameValuePair(
                ChangeTripleHandler.POST_BODY_TRIPLE,
                "<http://reegle.info/glossary/1111> <http://www.w3.org/2004/02/skos/core#prefLabel> \"Ottakringer Helles\"@en ."));

        post(new BasicNameValuePair(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD),
            new BasicNameValuePair(
                ChangeTripleHandler.POST_BODY_TRIPLE,
                "<http://reegle.info/glossary/1111> <http://www.w3.org/2004/02/skos/core#prefLabel> \"Schremser Edelmärzen\"@en ."));
    }

    private void addOtherConcept() throws IOException {
        post(new BasicNameValuePair(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD),
            new BasicNameValuePair(
                ChangeTripleHandler.POST_BODY_TRIPLE,
                "<http://reegle.info/glossary/1112> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2004/02/skos/core#Concept> ."));
    }

    private void linkConcepts() throws IOException {
        post(new BasicNameValuePair(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD),
            new BasicNameValuePair(
                ChangeTripleHandler.POST_BODY_TRIPLE,
                "<http://reegle.info/glossary/1111> <http://www.w3.org/2004/02/skos/core#related> <http://reegle.info/glossary/1112> ."));
    }

}
