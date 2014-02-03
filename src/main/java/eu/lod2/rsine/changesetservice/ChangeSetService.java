package eu.lod2.rsine.changesetservice;

import eu.lod2.rsine.remotenotification.RemoteChangeSetHandler;
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpException;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;

@Component
public class ChangeSetService implements ApplicationContextAware {

    private final Logger logger = LoggerFactory.getLogger(ChangeSetService.class);

    @Autowired
    private ChangeTripleHandler changeTripleHandler;

    private ApplicationContext applicationContext;

    private StopListener stopListener;
    private ServerSocket serverSocket;
    private Thread requestListenerThread;
    private boolean shouldStop;
    private int port = 2221;
    private String context = "";

    public ChangeSetService() {
        stopListener = new StopListener() {
            @Override
            public void hasStopped() {}
        };
    }

    public ChangeSetService(int port) {
        this();
        this.port = port;
    }

    public ChangeSetService(String context, int port) {
        this(port);
        this.context = context;
    }

    public void start() throws IOException {
        shouldStop = false;
        requestListenerThread = new RequestListenerThread(port);
        requestListenerThread.setDaemon(false);
        requestListenerThread.start();
    }

    public void stop() throws InterruptedException, IOException {
        shouldStop = true;

        serverSocket.close();
        requestListenerThread.join();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private class RequestListenerThread extends Thread {

        private SyncBasicHttpParams params;
        private HttpService httpService;
        private HttpRequestHandlerRegistry reqistry;

        RequestListenerThread(int port) throws IOException {
            serverSocket = new ServerSocket(port);

            setupParams();
            setupRequestHandler();

            httpService = new HttpService(
                new BasicHttpProcessor(),
                new DefaultConnectionReuseStrategy(),
                new DefaultHttpResponseFactory(),
                reqistry,
                params);
        }

        private void setupParams() {
            params = new SyncBasicHttpParams();
            params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
                .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
                .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
                .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
                .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");
        }

        private void setupRequestHandler() {
            reqistry = new HttpRequestHandlerRegistry();
            reqistry.register(context+"*", changeTripleHandler);
            reqistry.register(context+"/register", applicationContext.getBean(RegistrationHandler.class));
            reqistry.register(context+"/unregister", applicationContext.getBean(UnRegistrationHandler.class));
            reqistry.register(context+"/remote", applicationContext.getBean(RemoteChangeSetHandler.class));
            reqistry.register(context+"/feedback", applicationContext.getBean(FeedbackHandler.class));
        }

        @Override
        public void run() {
            logger.info("Listening on port " + serverSocket.getLocalPort());

            while (!shouldStop) {
                try {
                    // Set up HTTP connection
                    Socket socket = serverSocket.accept();
                    DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
                    logger.debug("Incoming connection from " + socket.getInetAddress());
                    conn.bind(socket, params);

                    // Start worker thread
                    WorkerThread t = new WorkerThread(httpService, conn);
                    t.setDaemon(true);
                    t.start();
                }
                catch (InterruptedIOException ex) {
                    // just continue
                }
                catch (IOException e) {
                    logger.info("I/O error initialising connection thread: "+ e.getMessage());
                    break;
                }
            }

            logger.info("service stopped");
            stopListener.hasStopped();
        }
    }

    public void setStopListener(StopListener stopListener) {
        this.stopListener = stopListener;
    }

    private class WorkerThread extends Thread {

        private final HttpService httpservice;
        private final HttpServerConnection conn;

        WorkerThread(final HttpService httpservice,
                     final HttpServerConnection conn)
        {
            super();

            this.httpservice = httpservice;
            this.conn = conn;
        }

        @Override
        public void run() {
            HttpContext context = new BasicHttpContext(null);
            try {
                while (!shouldStop && conn.isOpen()) {
                    httpservice.handleRequest(conn, context);
                }
                conn.shutdown();
            }
            catch (ConnectionClosedException ex) {
                logger.error("Client closed connection");
            }
            catch (IOException ex) {
                logger.error("I/O error: " + ex.getMessage());
            }
            catch (HttpException ex) {
                logger.error("Unrecoverable HTTP protocol violation: " + ex.getMessage());
            }
        }

    }

}
