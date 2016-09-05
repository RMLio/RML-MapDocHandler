package be.ugent.mmlab.rml.mapdochandler.extraction.std;

import be.ugent.mmlab.rml.mapdochandler.extraction.concrete.SourceExtractor;
import be.ugent.mmlab.rml.model.ReferenceFormulation;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.vocabularies.R2RMLVocabulary;
import be.ugent.mmlab.rml.vocabularies.RMLVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;


/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : StdSourceExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */
public abstract class StdSourceExtractor  implements SourceExtractor {

    // Log
    private static final Logger log = 
            LoggerFactory.getLogger(
            StdSourceExtractor.class.getSimpleName());

    

    /**
     *
     * @param term
     * @param resource
     * @param triplesMap
     * @return
     */
    protected RepositoryResult<Statement> getStatements(
            Repository repository, Enum term, Resource resource, TriplesMap triplesMap) {
        RepositoryResult<Statement> statements = null;
        try {
            URI p = getTermURI(repository, term);
            RepositoryConnection connection = repository.getConnection();
            statements = connection.getStatements(resource, p, null, true);

            connection.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return statements;
    }

    /**
     *
     * @param term
     * @return
     */
    protected static URI getTermURI(Repository repository, Enum term) {
        String namespace = R2RMLVocabulary.R2RML_NAMESPACE;

        if (term instanceof RMLVocabulary.RMLTerm) {
            namespace = RMLVocabulary.RML_NAMESPACE;
        } else if ((term instanceof R2RMLVocabulary.R2RMLTerm)) {
            namespace = R2RMLVocabulary.R2RML_NAMESPACE;
        } else {
            log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + term + " is not valid.");
        }

        RepositoryConnection connection;
        URI uri = null;
        try {
            connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();
            uri = vf.createURI(namespace + term);
            connection.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return uri;
    }
    
    /**
     *
     * @param repository
     * @param value
     * @return
     */
    @Override
    public ReferenceFormulation extractCustomReferenceFormulation(
            Repository repository, Value value){
            return null;
    }

}
