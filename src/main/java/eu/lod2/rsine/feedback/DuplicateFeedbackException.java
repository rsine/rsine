package eu.lod2.rsine.feedback;

public class DuplicateFeedbackException extends RuntimeException {

    private String msgId;

    public DuplicateFeedbackException(String msgId) {
        this.msgId = msgId;
    }

    @Override
    public String getMessage() {
        return "Feedback for message id " +msgId+ " already provided";
    }
}
