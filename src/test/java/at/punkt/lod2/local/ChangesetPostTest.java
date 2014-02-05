package at.punkt.lod2.local;

import com.jayway.awaitility.Awaitility;
import eu.lod2.rsine.changesetservice.ChangeSetService;
import eu.lod2.rsine.changesetservice.ChangeTripleHandler;
import eu.lod2.rsine.changesetstore.ChangeSetStore;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.*;
import org.junit.runner.RunWith;
import org.openrdf.OpenRDFException;
import org.openrdf.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"LocalTest-context.xml"})
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ChangesetPostTest {

    @Autowired
    private ChangeSetService changeSetService;

    @Autowired
    private ChangeSetStore changeSetStore;

    @Test
    public void postTripleChange() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en .");

        Assert.assertEquals(200, postChangeset(props));
    }    

    @Test
    public void postEmptyTriple() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE, "");

        Assert.assertEquals(400, postChangeset(props));
    }

    @Test
    public void postIllegalTriple() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE, "http://www.example.org/someconcept a skos:Concept .");

        Assert.assertEquals(400, postChangeset(props));
    }

    @Test
    public void postInvalidEofTriple() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en");

        Assert.assertEquals(400, postChangeset(props));
    }

    @Test
    public void postMissingTriple() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);

        Assert.assertEquals(400, postChangeset(props));
    }

    @Test
    public void postMissingChangeType() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en .");

        Assert.assertEquals(400, postChangeset(props));
    }

    /**
     * Posting an update results in creation of a changeset with both removal and addition statements
     */
    @Test
    public void postUpdate() throws OpenRDFException, IOException
    {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_UPDATE);
        props.setProperty(ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en .");
        props.setProperty(ChangeTripleHandler.POST_BODY_SECONDARYTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"updatedlabel\"@en .");

        int countBefore = changeSetStore.getChangeSetCount();
        postChangeset(props);

        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(new ChangeSetCountEquals(1));
        Assert.assertEquals(0, countBefore);
    }

    @Test
    public void tripleChangeToRepo() throws IOException, RepositoryException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en .");

        int changeSetsBefore = changeSetStore.getChangeSetCount();
        postChangeset(props);

        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(new ChangeSetCountEquals(changeSetsBefore + 1));
    }

    private int postChangeset(Properties properties) throws IOException {
        HttpPost httpPost = new HttpPost("http://localhost:2221");
        StringWriter sw = new StringWriter();
        properties.store(sw, null);
        httpPost.setEntity(new StringEntity(sw.toString()));
        HttpResponse response = new DefaultHttpClient().execute(httpPost);

        return response.getStatusLine().getStatusCode();
    }

    private class ChangeSetCountEquals implements Callable<Boolean> {

        private int targetValue;

        private ChangeSetCountEquals(int targetValue) {
            this.targetValue = targetValue;
        }

        @Override
        public Boolean call() throws Exception {
            return changeSetStore.getChangeSetCount() == targetValue;
        }

    }

}
