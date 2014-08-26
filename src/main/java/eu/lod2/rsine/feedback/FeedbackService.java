package eu.lod2.rsine.feedback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Service
public class FeedbackService {

    private final Logger logger = LoggerFactory.getLogger(FeedbackService.class);
    private final String DEFAULT_FEEDBACK_FILENAME = "/tmp/feedback.txt";

    private Set<String> msgIdsWithReceivedFeedback = new HashSet<String>();
    private String feedbackFileName = DEFAULT_FEEDBACK_FILENAME;

    public FeedbackService() {
        logger.warn("No feedback file name provided. Using '" +DEFAULT_FEEDBACK_FILENAME+ "'");
    }

    public FeedbackService(String feedbackFileName) throws IOException {
        this.feedbackFileName = feedbackFileName;
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
