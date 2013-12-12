package at.punkt.lod2.quality;

import at.punkt.lod2.util.Helper;
import eu.lod2.rsine.Rsine;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"FeedbackHandlerTest-context.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FeedbackHandlerTest {

    @Autowired
    public Rsine rsine;

    @Autowired
    private Helper helper;

    @Before
    public void setUp() throws IOException {
        rsine.start();
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        rsine.stop();
    }

    @Test
    public void processFeedback() {
        Assert.fail();
    }

    private int sendFeedbackRequest(String issueId, String issueResponse, String messageId) throws IOException {
        HttpGet httpGet = new HttpGet("http://localhost:" +helper.getChangeSetListeningPort()+
                "/feedback?" +issueId+
                "=" +issueResponse+
                "&msgid=" +messageId);

        HttpResponse response = new DefaultHttpClient().execute(httpGet);
        return response.getStatusLine().getStatusCode();
    }

}
