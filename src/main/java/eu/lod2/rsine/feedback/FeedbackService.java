package eu.lod2.rsine.feedback;

import eu.lod2.rsine.Rsine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.HashSet;
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

    public void handleFeedback(String issueId, String rating, String msgId) throws IOException {
        ensureNotDuplicateFeedback(msgId);

        BufferedWriter feedbackWriter = new BufferedWriter(new FileWriter(getOrCreateFeedbackFile(), true));
        feedbackWriter.append("IssueId: " +issueId+ ", rating: " +rating+ " (msgId: " +msgId+ ")\n");
        feedbackWriter.close();
        msgIdsWithReceivedFeedback.add(msgId);
    }

    private void ensureNotDuplicateFeedback(String msgId) {
        if (msgIdsWithReceivedFeedback.contains(msgId)) {
            throw new DuplicateFeedbackException(msgId);
        }
    }

    public File getOrCreateFeedbackFile() throws IOException {
        File feedbackFile = new File(feedbackFileName);
        if (!feedbackFile.exists()) {
            feedbackFile.createNewFile();
        }
        return feedbackFile;
    }

}
