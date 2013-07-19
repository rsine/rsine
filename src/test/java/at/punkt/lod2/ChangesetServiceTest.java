package at.punkt.lod2;

import changesetservice.ChangeTripleHandler;
import changesetservice.ChangesetService;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChangesetServiceTest {

    private ChangesetService changesetService;

    @Before
    public void setUp() throws IOException {
        changesetService = new ChangesetService(8080);
    }

    @After
    public void tearDown() {
        changesetService.stop();
    }

    @Test
    public void postTripleChange() throws IOException {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://localhost:8080");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair(ChangeTripleHandler.POST_BODY_CHANGETYPE, "add"));
        nvps.add(new BasicNameValuePair(ChangeTripleHandler.POST_BODY_SUBJECT, "http://example.org/myconcept"));
        nvps.add(new BasicNameValuePair(ChangeTripleHandler.POST_BODY_PREDICATE, "http://www.w3.org/2004/02/skos/core#prefLabel"));
        nvps.add(new BasicNameValuePair(ChangeTripleHandler.POST_BODY_OBJECT, "somelabel@en"));

        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        HttpResponse response = httpclient.execute(httpPost);

        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void tripleChangeToChangeset() {
        // TODO: post a triple and check if a RDF changeset is created
    }

    @Test
    public void tripleChangeToRepo() {
        // TODO: post a triple and check if repository is updated
    }


    }
