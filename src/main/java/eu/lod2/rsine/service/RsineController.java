package eu.lod2.rsine.service;

import eu.lod2.util.ItemNotFoundException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class RsineController {

    private final Logger logger = LoggerFactory.getLogger(RsineController.class);

    @Autowired
    private ChangeTripleService changeTripleService;

    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ResponseBody
    public void announceTriples(@RequestBody String announcedTriple, HttpServletResponse response) throws IOException {
        try {
            changeTripleService.handleAnnouncedTriple(announcedTriple);
        }
        catch (ItemNotFoundException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No triple or change type provided");
        }
        catch (RDFParseException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error parsing provided triple");
        }
        catch (RDFHandlerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    @RequestMapping(value = "register", method = RequestMethod.POST)
    @ResponseBody
    public void register() {
        //RegistrationHandler
    }

    @RequestMapping(value = "unregister", method = RequestMethod.POST)
    @ResponseBody
    public void unregister() {
        //UnRegistrationHandler
    }

    @RequestMapping(value = "remote", method = RequestMethod.POST)
    @ResponseBody
    public void remote() {
        //RemoteChangeSetHandler
    }

    @RequestMapping(value = "feedback", method = RequestMethod.POST)
    @ResponseBody
    public void feedback() {
        //FeedbackHandler
    }

}
