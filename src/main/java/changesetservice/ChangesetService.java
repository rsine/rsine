package changesetservice;

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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChangesetService {

    private final Logger logger = LoggerFactory.getLogger(ChangesetService.class);

    private boolean shoudStop;

    public ChangesetService(int port) throws IOException {
        Thread requestListenerThread = new RequestListenerThread(port);
        requestListenerThread.setDaemon(false);
        requestListenerThread.start();
    }

    public void stop() {
        shoudStop = true;
    }

    private class RequestListenerThread extends Thread {

        private ServerSocket serverSocket;
        private SyncBasicHttpParams params;
        private HttpService httpService;

        RequestListenerThread(int port) throws IOException {
            serverSocket = new ServerSocket(port);
            params = new SyncBasicHttpParams();

            // Set up request handlers
            HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
            reqistry.register("*", new ChangeTripleHandler());

            httpService = new HttpService(
                new BasicHttpProcessor(),
                new DefaultConnectionReuseStrategy(),
                new DefaultHttpResponseFactory(),
                reqistry,
                params);

            params
                    .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
                    .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
                    .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
                    .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
                    .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");

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
                } catch (InterruptedIOException ex) {
                    break;
                } catch (IOException e) {
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

        void cancel() {
            shoudStop = true;

            try {
                conn.shutdown();
            }
            catch (IOException e) {
                logger.error("Error shutting down connection");
            }
        }

        @Override
        public void run() {
            HttpContext context = new BasicHttpContext(null);
            try {
                while (!shoudStop && conn.isOpen()) {
                    httpservice.handleRequest(conn, context);
                }

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
