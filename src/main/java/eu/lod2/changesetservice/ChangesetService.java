package eu.lod2.changesetservice;

import eu.lod2.changesetstore.ChangeSetStore;
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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChangesetService {

    private final Logger logger = LoggerFactory.getLogger(ChangesetService.class);

    private ChangeSetCreator changeSetCreator;
    private ChangeSetStore changeSetStore;

    private ServerSocket serverSocket;
    private Thread requestListenerThread;
    private boolean shoudStop;

    public ChangesetService(int port) throws IOException, RepositoryException {
        changeSetCreator = new ChangeSetCreator();
        changeSetStore = new ChangeSetStore();

        requestListenerThread = new RequestListenerThread(port);
        requestListenerThread.setDaemon(false);
        requestListenerThread.start();
    }

    public void stop() throws InterruptedException, IOException {
        shoudStop = true;

        serverSocket.close();
        requestListenerThread.join();
    }

    public ChangeSetStore getChangeSetStore() {
        return changeSetStore;
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
            ChangeTripleHandler changeTripleHandler = new ChangeTripleHandler();
            changeTripleHandler.setChangeSetCreator(changeSetCreator);
            changeTripleHandler.setChangeSetStore(changeSetStore);

            reqistry = new HttpRequestHandlerRegistry();
            reqistry.register("*", changeTripleHandler);
        }

        @Override
        public void run() {
            logger.info("Listening on port " + serverSocket.getLocalPort());

            while (!shoudStop) {
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
                while (!shoudStop && conn.isOpen()) {
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
