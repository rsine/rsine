package eu.lod2.rsine.changesetservice;

import eu.lod2.rsine.registrationservice.NoSuchRegistrationError;
import eu.lod2.rsine.registrationservice.RegistrationService;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 *
 * @author http://www.turnguard.com/turnguard
 */
public class UnRegistrationHandler extends PostRequestHandler  {
    private RegistrationService registrationService;

    public UnRegistrationHandler(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }        
    
    @Override
    protected void handlePost(BasicHttpEntityEnclosingRequest request, HttpResponse response) {        
        try {
            String content = IOUtils.toString(request.getEntity().getContent());
            URI subscription = null;
            if(content!=null && !content.trim().isEmpty()){
                subscription = new URIImpl(content.split("=")[1]);
                try {
                    this.registrationService.unregister(subscription);
                } catch (NoSuchRegistrationError ex) {
                    response.setStatusCode(404);
                    response.setReasonPhrase(ex.getMessage());                    
                }
            } else {
                throw new IllegalArgumentException("content must conform to uri=");
            }
        } catch (IOException ex) {
            response.setStatusCode(500);
            response.setReasonPhrase(ex.getMessage());
        } catch (IllegalStateException ex) {
            response.setStatusCode(500);
            response.setReasonPhrase(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            response.setStatusCode(400);
            response.setReasonPhrase(ex.getMessage());
        }
    }

}
