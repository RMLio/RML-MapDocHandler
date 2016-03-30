package be.ugent.mmlab.rml.mapdochandler.extraction.source.concrete;

import be.ugent.mmlab.rml.mapdochandler.extraction.std.StdSourceExtractor;
import be.ugent.mmlab.rml.model.ReferenceFormulation;
import be.ugent.mmlab.rml.model.Source;
import be.ugent.mmlab.rml.model.source.std.StdApiSource;
import be.ugent.mmlab.rml.model.std.CsvwReferenceFormulation;
import be.ugent.mmlab.rml.vocabularies.CSVWVocabulary;
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
 * RML - Mapping Document Handler : CsvwExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */
public class CsvwExtractor extends StdSourceExtractor {
    
    // Log
    private static final Logger log = 
            LoggerFactory.getLogger(
            CsvwExtractor.class.getSimpleName());

    public CsvwExtractor() {
    }

    @Override
    public Set<Source> extractSources(Repository repository, Value value) {
        Set<Source> sources = new HashSet<Source>();
        log.debug("CSVW Value " + value);
        try {
            RepositoryConnection connection = repository.getConnection();
            
            RepositoryResult<Statement> statements = 
                    extractURL(repository, (Resource) value);
            
            if(statements.hasNext()){
                Statement statement = statements.next();
                
                Source source = 
                        extractSource(statement.getObject());
                sources.add(source);
            }
            else
                log.debug("Logical Sources statements from CSVW " + statements.hasNext());

            connection.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        log.debug("sources " + sources.size());
        return sources;
    }
    
    public Source extractSource(Value value) {
        log.debug("template " + value.stringValue());
        Source source = new StdApiSource(
                value.stringValue(), value.stringValue());
        return source;
    }
    
    public RepositoryResult<Statement> extractURL(
            Repository repository, Resource resource) {
        
        RepositoryResult<Statement> statements = null;
        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();
            URI predicate = vf.createURI(
                    CSVWVocabulary.CSVW_NAMESPACE + CSVWVocabulary.CSVWTerm.URL);
            
            statements = connection.getStatements(resource, predicate, null, true);

        } catch (RepositoryException ex) {
            log.error("Repository Exception " + ex);
        }
        return statements;
    }
    
    @Override
    public ReferenceFormulation extractCustomReferenceFormulation(
            Repository repository, Value value){
        ReferenceFormulation dialect = null;
        log.debug("Custom Reference formulation is triggered.");
        try {
            RepositoryConnection connection = repository.getConnection();
                ValueFactory vf = connection.getValueFactory();
            URI p = vf.createURI(
                    CSVWVocabulary.CSVW_NAMESPACE + CSVWVocabulary.CSVWTerm.DIALECT);
            RepositoryResult<Statement> dialectStatements =
                    connection.getStatements(
                    (Resource) value, p, null, true);
            
            if (dialectStatements.hasNext()) {
                log.debug("CSVW Custom Reference formulation is triggered.");
                Statement dialectStatement = dialectStatements.next();
                p = vf.createURI(
                    CSVWVocabulary.CSVW_NAMESPACE + CSVWVocabulary.CSVWTerm.DELIMITER);
                RepositoryResult<Statement> statements =
                    connection.getStatements(
                    (Resource) dialectStatement.getObject(), p, null, true);
                if(statements.hasNext()){
                    log.debug("Generating CSVW Referencing Formulation.");
                    dialect = new CsvwReferenceFormulation(
                            statements.next().getObject().stringValue());
                    log.debug("New CSVW Reference formulation was generated.");
                }
            }
            connection.close();
            
        } catch (RepositoryException ex) {
            log.error("Repository Exception " + ex);
        }
        
        return dialect;
    }
    
    public Value extractDelimiter(
            Repository repository, Resource resource) {
        Value delimiter = null;
        
        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();
            URI predicate = vf.createURI(
                    CSVWVocabulary.CSVW_NAMESPACE + CSVWVocabulary.CSVWTerm.DIALECT);
            //TODO: Fix the following: sub and obj same value
            RepositoryResult<Statement> statements =
                    connection.getStatements(resource, predicate, null, true);
            log.debug("Delimiter statements from CSVW " + statements.hasNext());
            Statement statement = statements.next();
            
            predicate = vf.createURI(
                    CSVWVocabulary.CSVW_NAMESPACE + CSVWVocabulary.CSVWTerm.DELIMITER);
            statements =
                    connection.getStatements(
                    (Resource) statement.getObject(), predicate, null, true);
            log.debug("Delimiter statements from CSVW " + statements.hasNext());
            
            statement = statements.next();
            
            delimiter = statement.getObject();

            connection.close();

        } catch (RepositoryException ex) {
            log.error("Repository Exception " + ex);
        }
        return delimiter;
    }

}
