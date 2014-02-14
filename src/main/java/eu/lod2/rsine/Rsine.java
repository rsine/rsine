package eu.lod2.rsine;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.security.InvalidParameterException;

@Component
public class Rsine {

    public final static String propertiesFileName = "application.properties";
    private final static Logger logger = LoggerFactory.getLogger(Rsine.class);

    public static CmdParams cmdParams;

    public static void main(String[] args) {
        try {
            cmdParams = new CmdParams(args);
            Server server = new Server(cmdParams.port);

            AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
            context.setConfigLocation("classpath:application-context.xml");

            ServletContextHandler contextHandler = new ServletContextHandler();
            contextHandler.setContextPath("/");
            contextHandler.addServlet(new ServletHolder(new DispatcherServlet(context)), "/");
            contextHandler.addEventListener(new ContextLoaderListener(context));
//            contextHandler.setResourceBase(new ClassPathResource("static_content").getURI().toString());
            server.setHandler(contextHandler);

            server.start();
        }
        catch (InvalidParameterException e) {
            logger.error("Insufficient parameters for starting the service");
        }
        catch (Exception e) {
            logger.error("Error starting rsine service", e);
        }
    }

}
