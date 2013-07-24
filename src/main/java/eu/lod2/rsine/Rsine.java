package eu.lod2.rsine;

import eu.lod2.rsine.changesetservice.ChangeSetCreator;
import eu.lod2.rsine.changesetservice.ChangeSetService;
import eu.lod2.rsine.changesetservice.RequestHandlerFactory;
import eu.lod2.rsine.changesetstore.ChangeSetStore;
import eu.lod2.rsine.querydispatcher.QueryDispatcher;
import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.registrationservice.Subscription;
import org.openrdf.repository.RepositoryException;

import java.io.IOException;

/**
 * Assembles an Rsine instance from its components
 * TODO: use spring ioc for this
 */
public class Rsine {

    private RegistrationService registrationService;

    public Rsine() throws IOException, RepositoryException {
        registrationService = new RegistrationService();

        ChangeSetService changeSetService = new ChangeSetService(8080);
        RequestHandlerFactory requestHandlerFactory = new RequestHandlerFactory();
        requestHandlerFactory.setChangeSetStore(new ChangeSetStore());
        requestHandlerFactory.setChangeSetCreator(new ChangeSetCreator());

        QueryDispatcher queryDispatcher = new QueryDispatcher();
        queryDispatcher.setRegistrationService(registrationService);
        requestHandlerFactory.setQueryDispatcher(queryDispatcher);

        changeSetService.setRequestHandlerFactory(requestHandlerFactory);

        changeSetService.start();
    }

    /**
     * @Deprecated registration should be exposed as an HTTP service
     */
    @Deprecated
    public Subscription requestSubscription() {
        return registrationService.requestSubscription();
    }

    /**
     * @Deprecated registration should be exposed as an HTTP service
     */
    @Deprecated
    public void registerSubscription(Subscription subscription) {
        registrationService.register(subscription);
    }

}
