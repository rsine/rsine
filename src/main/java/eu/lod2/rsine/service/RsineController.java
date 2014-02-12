package eu.lod2.rsine.service;

import eu.lod2.rsine.feedback.DuplicateFeedbackException;
import eu.lod2.rsine.feedback.FeedbackService;
import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.registrationservice.SubscriptionNotFoundException;
import eu.lod2.rsine.remotenotification.RemoteChangeSetService;
import eu.lod2.util.ItemNotFoundException;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.*;
import org.openrdf.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringReader;

@Controller
public class RsineController {

    private final Logger logger = LoggerFactory.getLogger(RsineController.class);

    @Autowired
    private ChangeTripleService changeTripleService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private RemoteChangeSetService remoteChangeSetService;

    @Autowired
    private FeedbackService feedbackService;

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
    public void register(@RequestHeader("content-type") String contentType,
                         @RequestBody String rdfRegistration,
                         HttpServletResponse response) throws IOException {
        try {
            RDFFormat format = Rio.getParserFormatForMIMEType(contentType);
            RDFParser parser = Rio.createParser(format);
            Model model = new TreeModel();
            StatementCollector handler = new StatementCollector(model);
            parser.setRDFHandler(handler);
            parser.parse(new StringReader(rdfRegistration), "");
            registrationService.register(model);
            response.setStatus(HttpServletResponse.SC_CREATED);
        }
        catch (OpenRDFException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not parse provided registration document");
        }
    }

    @RequestMapping(value = "unregister", method = RequestMethod.POST)
    @ResponseBody
    public void unregister(@RequestBody String resource,
                           HttpServletResponse response) throws IOException
    {
        try {
            this.registrationService.unregister(new URIImpl(resource));
        }
        catch (SubscriptionNotFoundException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Provided resource not registered");
        }
    }

    @RequestMapping(value = "remote", method = RequestMethod.POST)
    @ResponseBody
    public void remote(@RequestHeader("content-type") String contentType,
                       @RequestBody String rdfChangeSet,
                       HttpServletResponse response) throws IOException
    {
        try {
            remoteChangeSetService.handleChangeSet(rdfChangeSet, contentType);
        }
        catch (OpenRDFException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not parse provided changeset");
        }
    }

    @RequestMapping(value = "feedback", method = RequestMethod.POST)
    @ResponseBody
    public void feedback(HttpServletResponse response,
                         @RequestParam String issueId,
                         @RequestParam String rating,
                         @RequestParam String msgId) throws IOException
    {
        try {
            feedbackService.handleFeedback(issueId, rating, msgId);
            response.setStatus(HttpServletResponse.SC_OK);
        }
        catch (IOException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not access feedback file");
        }
        catch (DuplicateFeedbackException e) {

        }
    }

}
