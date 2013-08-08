package eu.lod2.rsine.dissemination.notifier.email;

import eu.lod2.rsine.dissemination.notifier.INotifier;
import eu.lod2.rsine.dissemination.notifier.NotifierDescriptor;
import eu.lod2.rsine.dissemination.notifier.NotifierParameters;
import eu.lod2.util.Namespaces;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;

public class EmailNotifierDescriptor implements NotifierDescriptor {

    public static final URI EMAIL_ADDRESS = new URIImpl("http://xmlns.com/foaf/0.1/mbox");

    @Override
    public URI getType() {
        return new URIImpl(Namespaces.RSINE_NAMESPACE.getName() + "emailNotifier");
    }

    @Override
    public NotifierParameters getParameters() {
        return new NotifierParameters().add(EMAIL_ADDRESS, XMLSchema.ANYURI, true);
    }

    @Override
    public INotifier create(NotifierParameters parameters) {
        return new EmailNotifier(parameters.getValueById(EMAIL_ADDRESS).stringValue());
    }

}
