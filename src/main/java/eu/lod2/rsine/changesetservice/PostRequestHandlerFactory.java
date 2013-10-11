package eu.lod2.rsine.changesetservice;

import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.remotenotification.RemoteChangeSetHandler;
import org.apache.http.protocol.HttpRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;

public class PostRequestHandlerFactory {

    private static PostRequestHandlerFactory instance = new PostRequestHandlerFactory();

    private PersistAndNotifyProvider persistAndNotifyProvider = new PersistAndNotifyProvider();

    @Autowired
    private ChangeSetCreator changeSetCreator;

    @Autowired
    private RegistrationService registrationService;
    
    public static PostRequestHandlerFactory getInstance() {
        return instance;
    }

    public PostRequestHandlerFactory() {
    }

    public HttpRequestHandler createChangeTripleHandler() {
        ChangeTripleHandler changeTripleHandler = new ChangeTripleHandler();
        changeTripleHandler.setChangeSetCreator(changeSetCreator);
        changeTripleHandler.setPersistAndNotifyProvider(persistAndNotifyProvider);
        return changeTripleHandler;
    }

    public HttpRequestHandler createRemoteChangeSetHandler() {
        RemoteChangeSetHandler remoteChangeSetHandler = new RemoteChangeSetHandler();
        remoteChangeSetHandler.setPersistAndNotifyProvider(persistAndNotifyProvider);
        return remoteChangeSetHandler;
    }

    public HttpRequestHandler createRegistrationHandler() {
        RegistrationHandler registrationHandler = new RegistrationHandler(registrationService);
        return registrationHandler;
    }
    
    public HttpRequestHandler createUnRegistrationHandler() {
        UnRegistrationHandler unregistrationHandler = new UnRegistrationHandler(registrationService);
        return unregistrationHandler;
    }

}
