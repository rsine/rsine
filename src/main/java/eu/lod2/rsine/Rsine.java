package eu.lod2.rsine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

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
            new ClassPathXmlApplicationContext("application-context.xml");
        }
        catch (InvalidParameterException e) {
            logger.error("Insufficient parameters for starting the service");
        }
    }

}
