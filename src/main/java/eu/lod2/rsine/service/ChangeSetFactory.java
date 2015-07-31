package eu.lod2.rsine.service;

import eu.lod2.util.Namespaces;
import org.openrdf.model.*;
import org.openrdf.model.impl.EmptyModel;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ChangeSetFactory {

    private final ValueFactory valueFactory = ValueFactoryImpl.getInstance();
    public enum StatementType {ADDITION, REMOVAL}

    public Model assembleChangeset(StatementType type, Statement... statements) {
        Collection<Statement> statsList = Arrays.asList(statements);
        switch (type) {
            case ADDITION: return assembleChangeset(statsList, Collections.EMPTY_LIST);
            case REMOVAL: return assembleChangeset(Collections.EMPTY_LIST, statsList);
        }

        return new EmptyModel(null);
    }

    public Model assembleChangeset(Collection<Statement> addedStatements,
                                   Collection<Statement> removedStatements)
    {
        Model model = new TreeModel(new HashSet<Namespace>(Arrays.asList(Namespaces.RSINE_NAMESPACE, Namespaces.CS_NAMESPACE)));

        URI changeSet = valueFactory.createURI(
            Namespaces.RSINE_NAMESPACE.getName(),
            "cs" +System.currentTimeMillis()+"_" +Math.round(Math.random() * 1000));

        model.add(new StatementImpl(changeSet,
            RDF.TYPE,
            valueFactory.createURI(Namespaces.CS_NAMESPACE.getName(), "ChangeSet")));
        model.add(new StatementImpl(changeSet,
            valueFactory.createURI(Namespaces.CS_NAMESPACE.getName(), "createdDate"),
            valueFactory.createLiteral(new Date())));
        model.add(new StatementImpl(changeSet,
            valueFactory.createURI(Namespaces.RSINE_NAMESPACE.getName(), "createdTimeStamp"),
            valueFactory.createLiteral(System.currentTimeMillis())));

        for (Statement addition : addedStatements) {
            addActionStatement(model, changeSet, addition, "addition");
        }

        for (Statement removal : removedStatements) {
            addActionStatement(model, changeSet, removal, "removal");
        }
        
        return model;
    }

    private void addActionStatement(Graph graph, Resource changeSet, Statement statement, String action) {
        graph.add(new StatementImpl(changeSet,
            valueFactory.createURI(Namespaces.CS_NAMESPACE.getName(), action),
            createStatementNode(statement, graph)));
    }

    private BNode createStatementNode(Statement affectedStatement, Graph graph) {
        BNode statementNode = valueFactory.createBNode();

        graph.add(new StatementImpl(statementNode, RDF.TYPE, RDF.STATEMENT));
        graph.add(new StatementImpl(statementNode, RDF.SUBJECT, affectedStatement.getSubject()));
        graph.add(new StatementImpl(statementNode, RDF.PREDICATE, affectedStatement.getPredicate()));
        graph.add(new StatementImpl(statementNode, RDF.OBJECT, affectedStatement.getObject()));

        return statementNode;
    }

}
