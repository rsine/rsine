package eu.lod2.rsine;

import eu.lod2.rsine.changesetservice.ChangeSetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.InvalidParameterException;

@Component
public class Rsine {

    public final static String propertiesFileName = "application.properties";
    private final static Logger logger = LoggerFactory.getLogger(Rsine.class);

    public static CmdParams cmdParams;

    public Rsine() {
    }

    public static void main(String[] args) {
        try {
            cmdParams = new CmdParams(args);
            ApplicationContext applicationContext = new ClassPathXmlApplicationContext("application-context.xml");
            ChangeSetService changeSetService = (ChangeSetService) applicationContext.getBean("changeSetService");
            changeSetService.start();
        }
        catch (IOException e) {
            logger.error("Error setting up network connection", e);
        }
        catch (InvalidParameterException e) {
            logger.error("Insufficient parameters for starting the service");
        }
    }

}
