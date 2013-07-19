package changesetservice;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class ChangeTripleHandler extends PostRequestHandler {

    private final Logger logger = LoggerFactory.getLogger(ChangeTripleHandler.class);

    public static String POST_BODY_CHANGETYPE = "changeType";
    public static String POST_BODY_SUBJECT = "subject";
    public static String POST_BODY_PREDICATE = "predicate";
    public static String POST_BODY_OBJECT = "object";

    @Override
    protected void handlePost(BasicHttpEntityEnclosingRequest request) {
        try {
            List<NameValuePair> params = URLEncodedUtils.parse(request.getEntity());
            // TODO: delegate creation of RDF changeset
            // TODO: store RDF changeset to Changeset Store
        }
        catch (IOException e) {
            logger.error("Error parsing request entity");
        }
    }
}
