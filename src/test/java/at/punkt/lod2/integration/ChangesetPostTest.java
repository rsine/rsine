package at.punkt.lod2.integration;

import eu.lod2.rsine.service.ChangeTripleService;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

/*
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"IntegrationTest-context.xml"})
*/
public class ChangesetPostTest {

    /*
    @Autowired
    private ChangeSetStore changeSetStore;
    */

    private static final int PORT = 2221;
    private static Server server;

    @BeforeClass
    public static void setUp() throws Exception {
        server = new Server(PORT);

        ContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        server.setHandler(context);

        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        dispatcherServlet.setContextConfigLocation("classpath:application-context.xml");

        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping(new ServletHolder(dispatcherServlet), "/*");

        context.setHandler(handler);

        server.start();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void postTripleChange() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleService.POST_BODY_CHANGETYPE, ChangeTripleService.CHANGETYPE_ADD);
        props.setProperty(ChangeTripleService.POST_BODY_AFFECTEDTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en .");

        Assert.assertEquals(200, postChangeset(props));
    }    

    @Test
    public void postEmptyTriple() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleService.POST_BODY_CHANGETYPE, ChangeTripleService.CHANGETYPE_ADD);
        props.setProperty(ChangeTripleService.POST_BODY_AFFECTEDTRIPLE, "");

        Assert.assertEquals(400, postChangeset(props));
    }

    @Test
    public void postIllegalTriple() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleService.POST_BODY_CHANGETYPE, ChangeTripleService.CHANGETYPE_ADD);
        props.setProperty(ChangeTripleService.POST_BODY_AFFECTEDTRIPLE, "http://www.example.org/someconcept a skos:Concept .");

        Assert.assertEquals(400, postChangeset(props));
    }

    @Test
    public void postInvalidEofTriple() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleService.POST_BODY_CHANGETYPE, ChangeTripleService.CHANGETYPE_ADD);
        props.setProperty(ChangeTripleService.POST_BODY_AFFECTEDTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en");

        Assert.assertEquals(400, postChangeset(props));
    }

    @Test
    public void postMissingTriple() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleService.POST_BODY_CHANGETYPE, ChangeTripleService.CHANGETYPE_ADD);

        Assert.assertEquals(400, postChangeset(props));
    }

    @Test
    public void postMissingChangeType() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleService.POST_BODY_AFFECTEDTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en .");

        Assert.assertEquals(400, postChangeset(props));
    }

    /*
    @Test
    public void postUpdate() throws OpenRDFException, IOException
    {
        Properties props = new Properties();
        props.setProperty(ChangeTripleService.POST_BODY_CHANGETYPE, ChangeTripleService.CHANGETYPE_UPDATE);
        props.setProperty(ChangeTripleService.POST_BODY_AFFECTEDTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en .");
        props.setProperty(ChangeTripleService.POST_BODY_SECONDARYTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"updatedlabel\"@en .");

        int countBefore = changeSetStore.getChangeSetCount();
        postChangeset(props);

        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(new ChangeSetCountEquals(countBefore + 1));
    }

    @Test
    public void tripleChangeToRepo() throws IOException, RepositoryException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleService.POST_BODY_CHANGETYPE, ChangeTripleService.CHANGETYPE_ADD);
        props.setProperty(ChangeTripleService.POST_BODY_AFFECTEDTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en .");

        int changeSetsBefore = changeSetStore.getChangeSetCount();
        postChangeset(props);

        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(new ChangeSetCountEquals(changeSetsBefore + 1));
    }
    */

    private int postChangeset(Properties properties) throws IOException {
        HttpPost httpPost = new HttpPost("http://localhost:" +PORT);
        StringWriter sw = new StringWriter();
        properties.store(sw, null);
        httpPost.setEntity(new StringEntity(sw.toString()));
        HttpResponse response = new DefaultHttpClient().execute(httpPost);

        return response.getStatusLine().getStatusCode();
    }

    /*
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
    */



}
