package eu.lod2.rsine.remotenotification;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import javax.naming.ServiceUnavailableException;
import java.io.IOException;
import java.net.URISyntaxException;

public class HttpHeaderRemoteServiceDetector implements IRemoteServiceDetector {

    private final String RSINE_URI_HEADERNAME = "rsineUrl";

    @Override
    public URI getRemoteService(Resource resource) throws ServiceUnavailableException {
        try {
            Header[] responseHeaders = getResponseHeaders(resource.stringValue());
            if (responseHeaders.length == 1) {
                return new URIImpl(responseHeaders[0].getValue());
            }
            else {
                throw new ServiceUnavailableException("No rsine uri for resource " +resource.toString()+ " found");
            }
        }
        catch (URISyntaxException e) {
            throw new ServiceUnavailableException("Resource " +resource.toString()+ " ist not a valid URI");
        }
        catch (IOException e) {
            throw new ServiceUnavailableException("Error connecting to remote concept hosting service: " +resource.toString());
        }
        catch (IllegalArgumentException e) {
            throw new ServiceUnavailableException("Received rsine uri not valid");
        }
    }

    private Header[] getResponseHeaders(String uri) throws URISyntaxException, IOException {
        HttpGet httpGet = new HttpGet(new java.net.URI(uri));
        HttpResponse response = new DefaultHttpClient().execute(httpGet);
        return response.getHeaders(RSINE_URI_HEADERNAME);
    }

}
