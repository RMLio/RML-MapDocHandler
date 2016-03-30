package be.ugent.mmlab.rml.mapdochandler.extraction.source.concrete;

import be.ugent.mmlab.rml.model.Source;
import be.ugent.mmlab.rml.mapdochandler.extraction.std.StdSourceExtractor;
import be.ugent.mmlab.rml.model.source.std.StdSparqlEndpointSource;
import be.ugent.mmlab.rml.vocabularies.SPARQLSDVocabulary;
import be.ugent.mmlab.rml.vocabularies.SPARQLSDVocabulary.SparqlSdTerm;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : SparqlExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */
public class SparqlExtractor extends StdSourceExtractor {

    // Log
    private static final Logger log = 
            LoggerFactory.getLogger(
            SparqlExtractor.class.getSimpleName());

    @Override
    public Set<Source> extractSources(Repository repository, Value value) {
        Set<Source> inputSources = new HashSet<Source>();
        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();

            URI predicate = vf.createURI(SPARQLSDVocabulary.SPARQLSD_NAMESPACE
                    + SPARQLSDVocabulary.SparqlSdTerm.ENDPOINT);
            RepositoryResult<Statement> statements =
                    connection.getStatements((Resource) value, predicate, null, true);

            while (statements.hasNext()) {
                Statement statement = statements.next();
                Source source = 
                        extractSource(repository, statement);
                inputSources.add(source);
            }
            connection.close();

        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return inputSources;
    }
    
    private Source extractSource(Repository repository, Statement statement){
        Source source ;
        String supportedLanguage = 
                exportSupportedLanguage(repository, statement.getSubject());
        String resultFormat = 
                exportResultFormat(repository, statement.getSubject());
        
        source = new StdSparqlEndpointSource(statement.getSubject().stringValue(), 
                statement.getObject().stringValue(),supportedLanguage, resultFormat);
        
        return source;
    }
    
    private String exportSupportedLanguage(Repository repository, Resource resource) {
        
        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();

            URI predicate = vf.createURI(SPARQLSDVocabulary.SPARQLSD_NAMESPACE
                    + SPARQLSDVocabulary.SparqlSdTerm.SUPPORTEDLANGUAGE);
            RepositoryResult<Statement> statements =
                    connection.getStatements(resource, predicate, null, true);
            if (statements.hasNext()) {
                Statement statement = statements.next();
                String supportedLanguage = statement.getObject().stringValue();
                switch(supportedLanguage){
                    case ("http://www.w3.org/ns/sparql-service-description#SPARQL10Query"):
                        return SparqlSdTerm.SPARQL10QUERY.toString();
                    case ("http://www.w3.org/ns/sparql-service-description#SPARQL11Query"):
                        return SparqlSdTerm.SPARQL11QUERY.toString();
                    case ("http://www.w3.org/ns/sparql-service-description#SPARQL11Update"):
                        return SparqlSdTerm.SPARQL11UPDATE.toString();  
                    default:
                        return null;
                }
            }
            
        } catch (RepositoryException ex) {
            log.error("Repository Exception " + ex);
        }
        return null;
    }
    
    private String exportResultFormat(Repository repository, Resource resource) {
        
        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();

            URI predicate = vf.createURI(SPARQLSDVocabulary.SPARQLSD_NAMESPACE
                    + SPARQLSDVocabulary.SparqlSdTerm.RESULTFORMAT);
            RepositoryResult<Statement> statements =
                    connection.getStatements(resource, predicate, null, true);
            if (statements.hasNext()) {
                Statement statement = statements.next();
                String resultFormat = statement.getObject().stringValue();
                switch(resultFormat){
                    case ("http://www.w3.org/ns/formats/data/SPARQL_Results_CSV"):
                        return SparqlSdTerm.SPARQL_RESULTS_CSV.toString();
                    case ("http://www.w3.org/ns/formats/data/SPARQL_Results_XML"):
                        return SparqlSdTerm.SPARQL_RESULTS_XML.toString();
                    case ("http://www.w3.org/ns/formats/data/SPARQL_Results_JSON"):
                        return SparqlSdTerm.SPARQL_RESULTS_JSON.toString();  
                    default:
                        return null;
                }
            }
            
        } catch (RepositoryException ex) {
            log.error("Repository Exception " + ex);
        }
        return null;
    }
}
