package eu.lod2.rsine.service;

import eu.lod2.rsine.Rsine;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

@Service
public class FeedbackService {

    private final Logger logger = LoggerFactory.getLogger(FeedbackService.class);

    private Set<String> msgIdsWithReceivedFeedback = new HashSet<String>();
    private String feedbackFileName;

    public FeedbackService() throws IOException {
        feedbackFileName = determineFeedbackFileName();
    }

    private String determineFeedbackFileName() throws IOException {
        Properties properties = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream(Rsine.propertiesFileName);

        properties.load(stream);
        return (String) properties.get("feedback.filename");
    }

    public void handleFeedback() {
            RequestParameters reqParams = new RequestParameters();
            reqParams.parse(request);

            if (msgIdsWithReceivedFeedback.contains(reqParams.msgId)) {
                logger.info("Feedback for message id " +reqParams.msgId+ " already provided; ignoring");
                response.setReasonPhrase("Feedback for this notification has already been provided");
            }
            else {
                BufferedWriter feedbackWriter = new BufferedWriter(new FileWriter(getOrCreateFeedbackFile(), true));
                feedbackWriter.append(reqParams.toString() + " (msgId: " +reqParams.msgId+ ")\n");
                feedbackWriter.close();
                msgIdsWithReceivedFeedback.add(reqParams.msgId);
            }
    }

    private void setResponseData(HttpResponse response, int statusCode, String responseText) {
        response.setStatusCode(statusCode);

        try {
            response.setEntity(new StringEntity(responseText));
        }
        catch (UnsupportedEncodingException e) {
            logger.error("Error setting feedback response entity", e);
        }
    }

    public File getOrCreateFeedbackFile() throws IOException {
        File feedbackFile = new File(feedbackFileName);
        if (!feedbackFile.exists()) {
            feedbackFile.createNewFile();
        }
        return feedbackFile;
    }

    private class RequestParameters {

        private final String ISSUE_ID_PARAM = "issueId";
        private final String RATING_PARAM = "rating";
        private final String MESSAGE_ID_PARAM = "msgId";

        String issueId, rating, msgId;

        void parse(HttpRequest request) throws URISyntaxException {
            List<NameValuePair> parameters = URLEncodedUtils.parse(new URI(
                request.getRequestLine().getUri()),
                HTTP.UTF_8);

            for (NameValuePair nameValuePair : parameters) {
                String paramName = nameValuePair.getName();
                if (paramName.equals(ISSUE_ID_PARAM)) {
                    issueId = nameValuePair.getValue();
                }
                else if (paramName.equals(RATING_PARAM)) {
                    rating = nameValuePair.getValue();
                }
                else if (paramName.equals(MESSAGE_ID_PARAM)) {
                    msgId = nameValuePair.getValue();
                }
            }
        }

        @Override
        public String toString() {
            return "IssueId: " +issueId+ ", rating: " +rating;
        }
    }

}
