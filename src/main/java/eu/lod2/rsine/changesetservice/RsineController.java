package eu.lod2.rsine.changesetservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RsineController {

    private final Logger logger = LoggerFactory.getLogger(RsineController.class);

    @Autowired
    private ChangeTripleService changeTripleService;

    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public void newTriple() {
        ChangeTripleService
    }

    @RequestMapping(value = "register", method = RequestMethod.POST)
    @ResponseBody
    public void register() {
        RegistrationHandler
    }

    @RequestMapping(value = "unregister", method = RequestMethod.POST)
    @ResponseBody
    public void unregister() {
        UnRegistrationHandler
    }

    @RequestMapping(value = "remote", method = RequestMethod.POST)
    @ResponseBody
    public void remote() {
        RemoteChangeSetHandler
    }

    @RequestMapping(value = "feedback", method = RequestMethod.POST)
    @ResponseBody
    public void feedback() {
        FeedbackHandler
    }

}
