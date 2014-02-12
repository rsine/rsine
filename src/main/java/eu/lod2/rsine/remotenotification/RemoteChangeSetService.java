package eu.lod2.rsine.remotenotification;

import eu.lod2.rsine.service.PersistAndNotifyProvider;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;

@Service
public class RemoteChangeSetService {

    private final Logger logger = LoggerFactory.getLogger(RemoteChangeSetService.class);

    @Autowired
    private PersistAndNotifyProvider persistAndNotifyProvider;

    public void handleChangeSet(String rdfChangeSet, String contentType)
        throws IOException, OpenRDFException
    {
        RDFFormat format = Rio.getParserFormatForMIMEType(contentType);
        Model changeSet = parseChangeSet(rdfChangeSet, format);
        persistAndNotifyProvider.persistAndNotify(changeSet, true);
    }

    private Model parseChangeSet(String rdfChangeSet, RDFFormat rdfFormat) throws IOException, OpenRDFException {
        RDFParser rdfParser = Rio.createParser(rdfFormat);
        Model changeSet = new TreeModel();
        StatementCollector collector = new StatementCollector(changeSet);
        rdfParser.setRDFHandler(collector);
        rdfParser.parse(new StringReader(rdfChangeSet), "");
        return changeSet;
    }

}
