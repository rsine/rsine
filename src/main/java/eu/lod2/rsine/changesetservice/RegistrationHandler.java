package eu.lod2.rsine.changesetservice;

import eu.lod2.rsine.registrationservice.RegistrationService;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.openrdf.model.Model;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;

/**
 *
 * @author http://www.turnguard.com/turnguard
 */
public class RegistrationHandler extends PostRequestHandler  {
    private RegistrationService registrationService;

    public RegistrationHandler(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }        
    
    @Override
    protected void handlePost(BasicHttpEntityEnclosingRequest request, HttpResponse response) {
        String contentType = request.getFirstHeader("content-type").getValue();
                
        try {
            RDFFormat format = Rio.getParserFormatForMIMEType(contentType);
            RDFParser parser = Rio.createParser(format);
            Model model = new TreeModel();
            StatementCollector handler = new StatementCollector(model);
            parser.setRDFHandler(handler); 
            parser.parse(request.getEntity().getContent(), "");            
            this.registrationService.register(model);
            response.setStatusCode(201);
        } catch (IOException ex) {            
            response.setStatusCode(503);
            response.setReasonPhrase(ex.getMessage());
        } catch (IllegalStateException ex) {            
            response.setStatusCode(503);
            response.setReasonPhrase(ex.getMessage());
        } catch (RDFHandlerException ex) {            
            response.setStatusCode(400);
            response.setReasonPhrase(ex.getMessage());
        } catch (RDFParseException ex) {            
            response.setStatusCode(400);
            response.setReasonPhrase(ex.getMessage());
        }
    }

}
