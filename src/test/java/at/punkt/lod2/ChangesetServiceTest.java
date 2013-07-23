package at.punkt.lod2;

import eu.lod2.changesetservice.ChangeSetCreator;
import eu.lod2.changesetservice.ChangeTripleHandler;
import eu.lod2.changesetservice.ChangesetService;
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
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ChangesetServiceTest {

    private ChangesetService changesetService;

    @Before
    public void setUp() throws IOException, RepositoryException {
        changesetService = new ChangesetService(8080);
    }

    @After
    public void tearDown() throws InterruptedException, IOException {
        changesetService.stop();
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
        nvps.add(new BasicNameValuePair(ChangeTripleHandler.POST_BODY_TRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en ."));

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
        nvps.add(new BasicNameValuePair(ChangeTripleHandler.POST_BODY_TRIPLE, ""));

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
        nvps.add(new BasicNameValuePair(ChangeTripleHandler.POST_BODY_TRIPLE, "http://www.example.org/someconcept a skos:Concept ."));

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
        nvps.add(new BasicNameValuePair(ChangeTripleHandler.POST_BODY_TRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en"));

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
            ChangeTripleHandler.POST_BODY_TRIPLE,
            "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en ."));

        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        return httpPost;
    }

    @Test
    public void tripleChangeToRepo() throws IOException, RepositoryException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.execute(createValidTripleChangePost());


        RepositoryConnection repCon = changesetService.getChangeSetStore().getRepository().getConnection();
        RepositoryResult<Statement> result = repCon.getStatements(
            null,
            RDF.TYPE,
            ValueFactoryImpl.getInstance().createURI(ChangeSetCreator.CS_NAMESPACE.getName(), "ChangeSet"),
            false);

        Assert.assertEquals(1, Iterations.asList(result).size());
    }

}
