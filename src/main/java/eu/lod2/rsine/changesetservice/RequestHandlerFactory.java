package eu.lod2.rsine.changesetservice;

import eu.lod2.rsine.changesetstore.ChangeSetStore;
import eu.lod2.rsine.querydispatcher.IQueryDispatcher;
import eu.lod2.rsine.remotenotification.RemoteNotificationService;
import org.apache.http.protocol.HttpRequestHandler;

public class RequestHandlerFactory {

    private ChangeSetCreator changeSetCreator;
    private ChangeSetStore changeSetStore;
    private IQueryDispatcher queryDispatcher;
    private RemoteNotificationService remoteNotificationService;

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

    public void setRemoteNotificationService(RemoteNotificationService remoteNotificationService) {
        this.remoteNotificationService = remoteNotificationService;
    }

}
