package be.ugent.mmlab.rml.mapdochandler.extraction.source.concrete;

import be.ugent.mmlab.rml.mapdochandler.extraction.std.StdSourceExtractor;
import be.ugent.mmlab.rml.model.Source;
import be.ugent.mmlab.rml.model.source.std.StdJdbcSource;
import be.ugent.mmlab.rml.vocabularies.D2RQVocabulary;
import be.ugent.mmlab.rml.vocabularies.D2RQVocabulary.D2RQTerm;
import java.util.HashSet;
import java.util.Set;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : JdbcExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */
public class JdbcExtractor extends StdSourceExtractor {

    // Log
    private static final Logger log = 
            LoggerFactory.getLogger(
            JdbcExtractor.class.getSimpleName());
       
    //TODO: The following does not actually iterate - change
    @Override
    public Set<Source> extractSources(Repository repository, Value value) {
            Set<Source> inputSources = new HashSet<Source>();
            Source source = extractSource (repository, value);
            
            inputSources.add(source);

            return inputSources;
    }
    
    public Source extractSource(Repository repository, Value value) {
        String jdbcDSN = extractJdbcTerm(repository, value, D2RQTerm.JDBCDSN);
        String jdbcDriver = extractJdbcTerm(repository, value, D2RQTerm.JDBCDRIVER);
        String username = extractJdbcTerm(repository, value, D2RQTerm.USERNAME);
        String password = extractJdbcTerm(repository, value, D2RQTerm.PASSWORD);
        Source source = 
                new StdJdbcSource(value.stringValue(), 
                jdbcDSN, jdbcDriver, username, password);
        return source;
    }
    
    private String extractJdbcTerm(Repository repository, Value resource, D2RQTerm term){
        String value = null;
        
        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();
            
            URI predicate = vf.createURI(
                        D2RQVocabulary.D2RQ_NAMESPACE + term);
            
            RepositoryResult<Statement> statements =
                    connection.getStatements((Resource) resource, predicate, null, true);
            
            while (statements.hasNext()) {
                value = statements.next().getObject().stringValue();
            }

            connection.close();
            
        } catch (RepositoryException ex) {
            log.error("Repository Exception " + ex);
        }
        return value;
    }
    
}
