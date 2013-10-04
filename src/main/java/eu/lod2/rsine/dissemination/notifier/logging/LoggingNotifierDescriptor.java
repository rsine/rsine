package eu.lod2.rsine.dissemination.notifier.logging;

import eu.lod2.rsine.dissemination.notifier.INotifier;
import eu.lod2.rsine.dissemination.notifier.NotifierDescriptor;
import eu.lod2.rsine.dissemination.notifier.NotifierParameters;
import eu.lod2.util.Namespaces;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class LoggingNotifierDescriptor implements NotifierDescriptor {

    @Override
    public URI getType() {
        return new URIImpl(Namespaces.RSINE_NAMESPACE.getName() + "loggingNotifier");
    }

    @Override
    public NotifierParameters getParameters() {
        return null;
    }

    @Override
    public INotifier create(NotifierParameters parameters) {
        return new LoggingNotifier();
    }

}
