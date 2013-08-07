package eu.lod2.rsine.dissemination.notifier;

import java.util.Collection;

public class EmailNotifier implements INotifier {

    private String emailAddress;

    public EmailNotifier(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @Override
    public void notify(Collection<String> messages) {
    }

}
