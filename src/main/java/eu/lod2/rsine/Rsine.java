package eu.lod2.rsine;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.DispatcherServlet;

import java.security.InvalidParameterException;

@Component
public class Rsine {

    public final static String propertiesFileName = "application.properties";
    private final static Logger logger = LoggerFactory.getLogger(Rsine.class);

    public static void main(String[] args) {
        try {
            CmdParams cmdParams = new CmdParams(args);
            Server server = new Server(cmdParams.port);

            ContextHandler context = new ServletContextHandler();
            context.setContextPath("/");
            server.setHandler(context);

            DispatcherServlet dispatcherServlet = new DispatcherServlet();
            dispatcherServlet.setContextConfigLocation("classpath:rsine-spring.xml");

            ServletHandler handler = new ServletHandler();
            handler.addServletWithMapping(new ServletHolder(dispatcherServlet), "/*");

            context.setHandler(handler);

            server.start();
        }
        catch (InvalidParameterException e) {
            logger.error("Insufficient parameters for starting the service");
        }
        catch (Exception e) {
            logger.error("Error starting rsine service");
        }
    }

}
