package eu.lod2.rsine;

import eu.lod2.rsine.changesetservice.ChangeSetService;
import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.rsine.remotenotification.RemoteNotificationServiceBase;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class Rsine {

    public final static String propertiesFileName = "application.properties";
    private final Logger logger = LoggerFactory.getLogger(Rsine.class);

    @Autowired
    private ChangeSetService changeSetService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private RemoteNotificationServiceBase remoteNotificationService;

    public static CmdParams cmdParams;

    public Rsine() {
    }

    public void start() throws IOException, RepositoryException {
        changeSetService.start();
    }

    public void stop() throws IOException, InterruptedException {
        changeSetService.stop();
    }

    public static void main(String[] args) throws IOException, RepositoryException {
        cmdParams = new CmdParams(args);
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("application-context.xml");
        Rsine rsine = (Rsine) applicationContext.getBean("rsine");
        rsine.start();
    }

    /**
     * @deprecated registration should be exposed as an HTTP service; for testing only
     */
    public void registerSubscription(Subscription subscription) {
        registrationService.register(subscription);
    }

}
