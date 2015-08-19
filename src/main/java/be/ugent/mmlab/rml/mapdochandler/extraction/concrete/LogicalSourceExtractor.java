/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : LogicalSourceExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */

package be.ugent.mmlab.rml.mapdochandler.extraction.concrete;

import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.vocabularies.QLVocabulary;
import be.ugent.mmlab.rml.vocabularies.QLVocabulary.QLTerm;
import be.ugent.mmlab.rml.vocabularies.RMLVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

public class LogicalSourceExtractor {
    
    // Log
    static final Logger log = LoggerFactory.getLogger(LogicalSourceExtractor.class);

    public Resource extractLogicalSource(
            Repository repository, Resource triplesMapSubject, TriplesMap triplesMap) {
        Resource blankLogicalSource = null;
        try {
            RepositoryConnection connection = repository.getConnection();

            RepositoryResult<Statement> logicalSourceStatements =
                    connection.getStatements(triplesMapSubject,
                    RMLTermExtractor.getTermURI(
                    repository, RMLVocabulary.RMLTerm.LOGICAL_SOURCE), null, true);

            blankLogicalSource = null;

            if (logicalSourceStatements != null) {
                blankLogicalSource = (Resource) logicalSourceStatements.next().getObject();
            }
            connection.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return blankLogicalSource;
    }

    /**
     *
     * @param rmlMappingGraph
     * @param triplesMapSubject
     * @param subject
     * @param triplesMap
     * @return
     */
    public QLTerm getReferenceFormulation(
            Repository repository, Resource triplesMapSubject,
            Resource subject, TriplesMap triplesMap) {
        RepositoryResult<Statement> statements;
        QLTerm term = null;
        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();

            URI logicalSource = vf.createURI(
                    RMLVocabulary.RML_NAMESPACE + RMLVocabulary.RMLTerm.REFERENCE_FORMULATION);

            statements = connection.getStatements(subject, logicalSource, null, true);
            
            if(statements.hasNext())
                term = QLVocabulary.getQLTerms(statements.next().getObject().stringValue());

            connection.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return term;
    }
    
    public String getIterator(
            Repository repository, Resource triplesMapSubject,
            Resource subject, TriplesMap triplesMap) {
        String term = null;
        RepositoryResult<Statement> statements;
        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();

            URI logicalSource = vf.createURI(
                    RMLVocabulary.RML_NAMESPACE + RMLVocabulary.RMLTerm.ITERATOR);
            statements = connection.getStatements(subject, logicalSource, null, true);
            
            if(statements.hasNext())
                term = statements.next().getObject().stringValue();
            connection.close();

        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return term;
    }
}
