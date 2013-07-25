package at.punkt.lod2;

import eu.lod2.rsine.changesetservice.ChangeSetCreator;
import eu.lod2.rsine.changesetservice.ChangeSetService;
import eu.lod2.rsine.changesetservice.ChangeTripleHandler;
import eu.lod2.rsine.changesetservice.RequestHandlerFactory;
import eu.lod2.rsine.changesetstore.ChangeSetStore;
import eu.lod2.rsine.querydispatcher.IQueryDispatcher;
import eu.lod2.util.Namespaces;
import info.aduna.iteration.Iterations;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ChangesetServiceTest {

    private ChangeSetService changeSetService;
    private ChangeSetStore changeSetStore;

    @Before
    public void setUp() throws IOException, RepositoryException {
        RequestHandlerFactory requestHandlerFactory = new RequestHandlerFactory();
        requestHandlerFactory.setChangeSetCreator(new ChangeSetCreator());
        requestHandlerFactory.setQueryDispatcher(new DummyQueryDispatcher());

        changeSetStore = new ChangeSetStore();
        requestHandlerFactory.setChangeSetStore(changeSetStore);

        changeSetService = new ChangeSetService(8080);
        changeSetService.setRequestHandlerFactory(requestHandlerFactory);

        changeSetService.start();
    }

    @After
    public void tearDown() throws InterruptedException, IOException {
        changeSetService.stop();
    }

    @Test
    public void postTripleChange() throws IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(createValidTripleChangePost());

        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
    }

    private HttpPost createValidTripleChangePost() throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost("http://localhost:8080");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD));
        nvps.add(new BasicNameValuePair(ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en ."));

        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        return httpPost;
    }

    @Test
    public void postEmptyTriple() throws IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(createEmptyTripleChangePost());

        Assert.assertEquals(400, response.getStatusLine().getStatusCode());
    }

    private HttpPost createEmptyTripleChangePost() throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost("http://localhost:8080");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD));
        nvps.add(new BasicNameValuePair(ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE, ""));

        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        return httpPost;
    }

    @Test
    public void postIllegalTriple() throws IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(createIllegalTripleChangePost());

        Assert.assertEquals(400, response.getStatusLine().getStatusCode());
    }

    private HttpPost createIllegalTripleChangePost() throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost("http://localhost:8080");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD));
        nvps.add(new BasicNameValuePair(ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE, "http://www.example.org/someconcept a skos:Concept ."));

        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        return httpPost;
    }

    @Test
    public void postInvalidEofTriple() throws IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(createInvalidEofTripleChangePost());

        Assert.assertEquals(400, response.getStatusLine().getStatusCode());
    }

    private HttpPost createInvalidEofTripleChangePost() throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost("http://localhost:8080");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD));
        nvps.add(new BasicNameValuePair(ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en"));

        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        return httpPost;
    }

    @Test
    public void postMissingTriple() throws IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(createMissingTripleChangePost());

        Assert.assertEquals(400, response.getStatusLine().getStatusCode());
    }

    private HttpPost createMissingTripleChangePost() throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost("http://localhost:8080");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair(ChangeTripleHandler.POST_BODY_CHANGETYPE, "add"));

        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        return httpPost;
    }

    /**
     * Posting an update results in creation of a changeset with both removal and addition statements
     */
    @Test
    public void postUpdate()
        throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException
    {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.execute(createUpdatePost());

        RepositoryConnection repCon = changeSetStore.getRepository().getConnection();
        TupleQueryResult result = repCon.prepareTupleQuery(QueryLanguage.SPARQL,
                Namespaces.SKOS_PREFIX +
                        Namespaces.CS_PREFIX +
                        "SELECT * " +
                        "FROM NAMED <" + Namespaces.CHANGESET_CONTEXT + "> " +
                        "WHERE {" +
                        "GRAPH ?g {" +
                            "?cs a cs:ChangeSet . " +
                            "?cs cs:removal ?removal . " +
                            "?cs cs:addition ?addition . " +
                            "}" +
                        "}").evaluate();

        Assert.assertTrue(result.hasNext());
    }

    private HttpPost createUpdatePost() throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost("http://localhost:8080");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_UPDATE));
        nvps.add(new BasicNameValuePair(ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en ."));
        nvps.add(new BasicNameValuePair(ChangeTripleHandler.POST_BODY_SECONDARYTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"updatedlabel\"@en ."));

        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        return httpPost;
    }

    @Test
    public void postMissingChangeType() throws IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(createMissingChangeTypePost());

        Assert.assertEquals(400, response.getStatusLine().getStatusCode());
    }

    private HttpPost createMissingChangeTypePost() throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost("http://localhost:8080");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair(
            ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE,
            "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en ."));

        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        return httpPost;
    }

    @Test
    public void tripleChangeToRepo() throws IOException, RepositoryException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.execute(createValidTripleChangePost());

        RepositoryConnection repCon = changeSetStore.getRepository().getConnection();
        RepositoryResult<Statement> result = repCon.getStatements(
            null,
            RDF.TYPE,
            ValueFactoryImpl.getInstance().createURI(Namespaces.CS_NAMESPACE.getName(), "ChangeSet"),
            false);

        Assert.assertEquals(1, Iterations.asList(result).size());
    }

    private class DummyQueryDispatcher implements IQueryDispatcher {

        @Override
        public void trigger() {
            //don't do nothing here
        }

    }
}
