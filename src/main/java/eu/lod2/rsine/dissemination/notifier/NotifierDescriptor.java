package eu.lod2.rsine.dissemination.notifier;

import org.openrdf.model.URI;

public interface NotifierDescriptor {

    public URI getType();
    public NotifierParameters getParameters();
    public INotifier create(NotifierParameters parameters);

}
