package at.punkt.lod2.integration;

import eu.lod2.rsine.Rsine;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FeedbackServiceTest {

    private Server server;
    private File feedbackFile;
    private final String FEEDBACKFILE_NAME = "/tmp/feedbackTest.txt";

    @Before
    public void setUp() throws Exception {
        server = Rsine.initAndStart(2221, "test", null, FEEDBACKFILE_NAME);
        feedbackFile = new File(FEEDBACKFILE_NAME);
        feedbackFile.createNewFile();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        feedbackFile.delete();
    }

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
        BufferedReader reader = new BufferedReader(new FileReader(feedbackFile));
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
