package be.ugent.mmlab.rml.mapdochandler.extraction.source.concrete;

import be.ugent.mmlab.rml.mapdochandler.extraction.std.StdSourceExtractor;
import be.ugent.mmlab.rml.model.Source;
import be.ugent.mmlab.rml.model.source.std.StdApiSource;
import be.ugent.mmlab.rml.model.source.std.StdSparqlEndpointSource;
import be.ugent.mmlab.rml.vocabularies.VoIDVocabulary;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : VoidExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */
public class VoidExtractor extends StdSourceExtractor {
    
    // Log
    private static final Logger log = 
            LoggerFactory.getLogger(
            VoidExtractor.class.getSimpleName());

    public VoidExtractor() {
    }

    @Override
    public Set<Source> extractSources(Repository repository, Value value) {
        Set<Source> inputSources = new HashSet<Source>();
        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();
            URI predicate = vf.createURI(
                    VoIDVocabulary.VOID_NAMESPACE + VoIDVocabulary.VoIDTerm.SPARQLENDPOINT);
            //TODO: Check the following: sub and obj same value
            RepositoryResult<Statement> statements =
                    connection.getStatements((Resource) value, predicate, null, true);
            
            while (statements.hasNext()) {
                Statement statement = statements.next();
                Source source = extractSparqlSource(statement);
                inputSources.add(source);
            }

            if (!statements.hasNext()) {
                predicate = vf.createURI(
                        VoIDVocabulary.VOID_NAMESPACE + VoIDVocabulary.VoIDTerm.DATADUMP);
                //TODO: Check the following: sub and obj same value
                statements = connection.getStatements((Resource) value, predicate, null, true);
            }

            while (statements.hasNext()) {
                inputSources.add(
                        new StdApiSource(
                        value.stringValue(), statements.next().getObject().stringValue()));
            }
            connection.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return inputSources;
    }
    
    private Source extractSparqlSource(Statement statement){
        Source source = new StdSparqlEndpointSource(
                statement.getSubject().stringValue(), statement.getObject().stringValue());
        return source;
    }
    
    private Source extractDataDumpSource(Statement statement){
        Source source = new StdApiSource(
                statement.getSubject().stringValue(), statement.getObject().stringValue());
        return source;
        
    }

}
