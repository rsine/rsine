package eu.lod2.rsine.changesetservice;

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
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;

@Component
public class ChangeSetService {

    private final Logger logger = LoggerFactory.getLogger(ChangeSetService.class);

    @Autowired
    private ChangeTripleHandler changeTripleHandler;

    private ServerSocket serverSocket;
    private Thread requestListenerThread;
    private boolean shouldStop;
    private int port;

    public ChangeSetService(int port) {
        this.port = port;
    }

    public void start() throws IOException, RepositoryException {
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

    private class RequestListenerThread extends Thread {

        private SyncBasicHttpParams params;
        private HttpService httpService;
        private HttpRequestHandlerRegistry reqistry;

        RequestListenerThread(int port) throws IOException, RepositoryException {
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

        private void setupRequestHandler() throws RepositoryException {
            reqistry = new HttpRequestHandlerRegistry();
            reqistry.register("*", changeTripleHandler);
            reqistry.register("/register", PostRequestHandlerFactory.getInstance().createRegistrationHandler());
            reqistry.register("/unregister", PostRequestHandlerFactory.getInstance().createUnRegistrationHandler());
            reqistry.register("/remote", PostRequestHandlerFactory.getInstance().createRemoteChangeSetHandler());                        
        }

        @Override
        public void run() {
            logger.info("Listening on port " + serverSocket.getLocalPort());

            while (!shouldStop) {
                try {
                    // Set up HTTP connection
                    Socket socket = serverSocket.accept();
                    DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
                    logger.info("Incoming connection from " + socket.getInetAddress());
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
        }

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
