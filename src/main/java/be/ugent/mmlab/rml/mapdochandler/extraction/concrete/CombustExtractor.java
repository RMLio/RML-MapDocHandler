
package be.ugent.mmlab.rml.mapdochandler.extraction.concrete;

import be.ugent.mmlab.rml.vocabularies.CoVocabulary;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RML Processor
 *
 * @author andimou
 */
public class CombustExtractor {
    // Log
    static final Logger log = LoggerFactory.getLogger(
            CombustExtractor.class.getSimpleName());
    
    public boolean exrtactValidate(Resource term, Repository repository) {
        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();

            URI uri = vf.createURI(CoVocabulary.CO_NAMESPACE
                    + CoVocabulary.COTerm.REQUIRES);
            URI obj = vf.createURI(CoVocabulary.CO_NAMESPACE
                    + CoVocabulary.COTerm.VERIFICATION_CLASS);

            RepositoryResult<Statement> validation_statements =
                    connection.getStatements(term, uri, obj, true);

            if (validation_statements.hasNext()) {
                log.debug("Term Map to be validated");
                return true;
            }

        } catch (RepositoryException ex) {
            log.error("Repository Exception " + ex);
        }
        return false;
    }
    
    public boolean exrtactComplete(Resource term, Repository repository){
        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();

            URI uri = vf.createURI(CoVocabulary.CO_NAMESPACE
                    + CoVocabulary.COTerm.REQUIRES);
            URI obj = vf.createURI(CoVocabulary.CO_NAMESPACE
                    + CoVocabulary.COTerm.COMPLETION_CLASS);
            
            RepositoryResult<Statement> statements = 
                    connection.getStatements(term, uri, obj, true);

                if(statements.hasNext()){
                    log.debug("Term Map to be completed");
                    return true;
                }
            
        } catch (RepositoryException ex) {
            log.error("Repository Exception " + ex);
        }
        return false;
    }

}
