package eu.lod2.rsine.changesetservice;

import eu.lod2.rsine.changesetstore.ChangeSetStore;
import eu.lod2.rsine.querydispatcher.IQueryDispatcher;
import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.remotenotification.RemoteChangeSetHandler;
import eu.lod2.rsine.remotenotification.RemoteNotificationServiceBase;
import org.apache.http.protocol.HttpRequestHandler;

public class PostRequestHandlerFactory {

    private static PostRequestHandlerFactory instance = new PostRequestHandlerFactory();

    private PersistAndNotifyProvider persistAndNotifyProvider = new PersistAndNotifyProvider();
    private ChangeSetCreator changeSetCreator;
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
        RegistrationHandler registrationHandler = new RegistrationHandler(this.registrationService);        
        return registrationHandler;
    }
    
    public HttpRequestHandler createUnRegistrationHandler() {
        UnRegistrationHandler registrationHandler = new UnRegistrationHandler(this.registrationService);        
        return registrationHandler;
    }
    
    public void setChangeSetCreator(ChangeSetCreator changeSetCreator) {
        this.changeSetCreator = changeSetCreator;
    }

    public void setChangeSetStore(ChangeSetStore changeSetStore) {
        persistAndNotifyProvider.setChangeSetStore(changeSetStore);
    }

    public void setQueryDispatcher(IQueryDispatcher queryDispatcher) {
        persistAndNotifyProvider.setQueryDispatcher(queryDispatcher);
    }

    public void setRemoteNotificationService(RemoteNotificationServiceBase remoteNotificationService) {
        persistAndNotifyProvider.setRemoteNotificationService(remoteNotificationService);
    }

    public void setRegistrationService(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

}
