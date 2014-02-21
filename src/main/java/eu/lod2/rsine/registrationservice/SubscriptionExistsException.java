package eu.lod2.rsine.registrationservice;

public class SubscriptionExistsException extends RuntimeException {

    public SubscriptionExistsException(String message) {
        super(message);
    }
}
