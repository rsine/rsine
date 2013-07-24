package eu.lod2.rsine.changesetservice;

import eu.lod2.rsine.changesetstore.ChangeSetStore;
import eu.lod2.rsine.querydispatcher.IQueryDispatcher;
import eu.lod2.rsine.querydispatcher.QueryDispatcher;
import org.apache.http.protocol.HttpRequestHandler;

public class RequestHandlerFactory {

    private ChangeSetCreator changeSetCreator;
    private ChangeSetStore changeSetStore;
    private IQueryDispatcher queryDispatcher;

    public HttpRequestHandler createChangeTripleHandler() {
        ChangeTripleHandler changeTripleHandler = new ChangeTripleHandler();
        changeTripleHandler.setChangeSetCreator(changeSetCreator);
        changeTripleHandler.setChangeSetStore(changeSetStore);
        changeTripleHandler.setQueryDispatcher(queryDispatcher);
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

}
