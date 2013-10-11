package eu.lod2.rsine.remotenotification;

import eu.lod2.util.Namespaces;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.openrdf.model.*;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.ntriples.NTriplesWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

public class RemoteNotificationService extends RemoteNotificationServiceBase {

    private final Logger logger = LoggerFactory.getLogger(RemoteNotificationService.class);
    private String authoritativeUri;

    public RemoteNotificationService(String authoritativeUri) {
        this.authoritativeUri = authoritativeUri;
    }

    @Override
    public void announce(Model changeSet) {
        Collection<Resource> extResources = getExternalResources(changeSet);

        for (Resource extResource : extResources) {
            URI remoteService = remoteServiceDetector.getRemoteService(extResource);
            addSourceInfo(changeSet);

            try {
                String ntriplesChangeSet = createNTriplesChangeSet(changeSet);
                postChangeSet(remoteService, ntriplesChangeSet);
            }
            catch (IOException e) {
                logger.error("Error posting changeset to '" +remoteService.stringValue()+ "': " +e.getMessage());
            }
            catch (RDFHandlerException e) {
                logger.error("Error serializing changeset", e);
            }
        }
    }

    private Collection<Resource> getExternalResources(Model changeSet) {
        ValueFactory valueFactory = new ValueFactoryImpl();
        Resource additionNode = changeSet.filter(null,
            valueFactory.createURI(Namespaces.CS_NAMESPACE.getName(), "addition"),
            null).objectResource();
        Resource additionStatement = changeSet.filter(additionNode, RDF.STATEMENT, null).objectResource();

        Resource subject = changeSet.filter(additionStatement, RDF.SUBJECT, null).objectResource();
        Resource object = changeSet.filter(additionStatement, RDF.OBJECT, null).objectResource();

        return filterExternalResources(subject, object);
    }

    private Collection<Resource> filterExternalResources(Resource... resources) {
        Collection<Resource> externalResources = new ArrayList<Resource>();
        for (Resource resource : resources) {
            if (!resource.stringValue().toUpperCase().contains(authoritativeUri.toUpperCase())) {
                externalResources.add(resource);

                logger.info("Recognized resource '" +resource.stringValue()+ " as external");
            }
        }
        return externalResources;
    }

    private void addSourceInfo(Model changeSet) {
        ValueFactory valueFactory = new ValueFactoryImpl();
        Resource changeSetRoot = changeSet.filter(null,
            valueFactory.createURI(Namespaces.CS_NAMESPACE.getName(), "createdDate"),
            null).subjects().iterator().next();
        changeSet.add(valueFactory.createStatement(
            changeSetRoot,
            valueFactory.createURI(Namespaces.DCTERMS_NAMESPACE.getName(), "source"),
            valueFactory.createURI(authoritativeUri)));
    }

    private String createNTriplesChangeSet(Model changeSet) throws RDFHandlerException {
        StringWriter sw = new StringWriter();
        RDFWriter writer = new NTriplesWriterFactory().getWriter(sw);

        writer.startRDF();
        for (Statement st : changeSet) {
            writer.handleStatement(st);
        }
        writer.endRDF();

        return sw.toString();
    }

    private void postChangeSet(URI remoteService, String changeSet) throws IOException, RDFHandlerException {
        HttpPost httpPost = new HttpPost(remoteService.stringValue());
        httpPost.setEntity(new StringEntity(changeSet));
        HttpResponse response = new DefaultHttpClient().execute(httpPost);

        logger.info("Posted changeset to '" +remoteService+ "', response: " +response.getStatusLine().getStatusCode());
    }

}
