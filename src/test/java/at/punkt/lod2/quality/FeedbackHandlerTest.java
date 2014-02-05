package at.punkt.lod2.quality;

import at.punkt.lod2.util.Helper;
import eu.lod2.rsine.changesetservice.FeedbackHandler;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"FeedbackHandlerTest-context.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FeedbackHandlerTest {

    @Autowired
    private Helper helper;

    @Autowired
    private FeedbackHandler feedbackHandler;

    @Test
    public void feedbackLogged() throws IOException {
        long feedbackLinesBefore = getFeedbackFileLines();
        sendFeedbackRequest("chr", "1", "12345");
        Assert.assertEquals(1, getFeedbackFileLines() - feedbackLinesBefore);
    }

    @Test
    public void feedbackLoggedOnlyOnceForSameMsgId() throws IOException {
        long feedbackLinesBefore = getFeedbackFileLines();
        sendFeedbackRequest("chr", "1", "12345");
        sendFeedbackRequest("chr", "2", "12345");
        Assert.assertEquals(1, getFeedbackFileLines() - feedbackLinesBefore);
    }

    @Test
    public void feedbackLoggedDifferentMsgId() throws IOException {
        long feedbackLinesBefore = getFeedbackFileLines();
        sendFeedbackRequest("chr", "1", "12345");
        sendFeedbackRequest("chr", "2", "54321");
        Assert.assertEquals(2, getFeedbackFileLines() - feedbackLinesBefore);
    }

    private long getFeedbackFileLines() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(feedbackHandler.getOrCreateFeedbackFile()));
        int lines = 0;
        while (reader.readLine() != null) lines++;
        reader.close();
        return lines;
    }

    private int sendFeedbackRequest(String issueId, String rating, String messageId) throws IOException {
        HttpGet httpGet = new HttpGet("http://localhost:2221"+
                "/feedback?issueId=" +issueId+
                "&rating=" +rating+
                "&msgId=" +messageId);

        HttpResponse response = new DefaultHttpClient().execute(httpGet);
        return response.getStatusLine().getStatusCode();
    }

}
