package eu.lod2.rsine.changesetservice;

import eu.lod2.rsine.changesetstore.ChangeSetStore;
import eu.lod2.rsine.querydispatcher.IQueryDispatcher;
import eu.lod2.rsine.remotenotification.RemoteChangeSetHandler;
import eu.lod2.rsine.remotenotification.RemoteNotificationServiceBase;
import org.apache.http.protocol.HttpRequestHandler;

public class RequestHandlerFactory {

    private static final RequestHandlerFactory instance = new RequestHandlerFactory();

    private ChangeSetCreator changeSetCreator;
    private ChangeSetStore changeSetStore;
    private IQueryDispatcher queryDispatcher;
    private RemoteNotificationServiceBase remoteNotificationService;

    private RequestHandlerFactory() {
    }

    public static RequestHandlerFactory getInstance() {
        return instance;
    }

    public HttpRequestHandler createChangeTripleHandler() {
        ChangeTripleHandler changeTripleHandler = new ChangeTripleHandler();
        changeTripleHandler.setChangeSetCreator(changeSetCreator);
        changeTripleHandler.setChangeSetStore(changeSetStore);
        changeTripleHandler.setQueryDispatcher(queryDispatcher);
        changeTripleHandler.setRemoteNotificationService(remoteNotificationService);
        return changeTripleHandler;
    }

    public void setChangeSetCreator(ChangeSetCreator changeSetCreator) {
        this.changeSetCreator = changeSetCreator;
    }

    public void setChangeSetStore(ChangeSetStore changeSetStore) {
        this.changeSetStore = changeSetStore;
    }

    public void setQueryDispatcher(IQueryDispatcher queryDispatcher) {
        this.queryDispatcher = queryDispatcher;
    }

    public void setRemoteNotificationService(RemoteNotificationServiceBase remoteNotificationService) {
        this.remoteNotificationService = remoteNotificationService;
    }

    public HttpRequestHandler createRemoteChangeSetHandler() {
        RemoteChangeSetHandler remoteChangeSetHandler = new RemoteChangeSetHandler();
        remoteChangeSetHandler.setQueryDispatcher(queryDispatcher);
        remoteChangeSetHandler.setChangeSetStore(changeSetStore);
        return remoteChangeSetHandler;
    }

}
