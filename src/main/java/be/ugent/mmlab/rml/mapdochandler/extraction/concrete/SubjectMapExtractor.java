package be.ugent.mmlab.rml.mapdochandler.extraction.concrete;

import be.ugent.mmlab.rml.mapdochandler.extraction.condition.ConditionPredicateObjectMapExtractor;
import be.ugent.mmlab.rml.model.RDFTerm.GraphMap;
import be.ugent.mmlab.rml.model.RDFTerm.SubjectMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdConditionSubjectMap;
import be.ugent.mmlab.rml.model.std.StdSubjectMap;
import be.ugent.mmlab.rml.vocabularies.CRMLVocabulary;
import be.ugent.mmlab.rml.vocabularies.R2RMLVocabulary;
import java.util.Set;
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

/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : SubjectMapExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */
public class SubjectMapExtractor extends StdTermMapExtractor {
    
    // Log
    static final Logger log = 
            LoggerFactory.getLogger(
            SubjectMapExtractor.class.getSimpleName());
    
    public SubjectMap extractSubjectMap(
            Repository repository, Resource triplesMapSubject,
            Set<GraphMap> savedGraphMaps, TriplesMap triplesMap) {
        SubjectMap result = null;
        RepositoryResult<Statement> statements;
        log.debug("Extracting Subject Map...");
        
        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();
            // Extract subject map
            statements = connection.getStatements(
                    triplesMapSubject, RMLTermExtractor.getTermURI(
                    repository, R2RMLVocabulary.R2RMLTerm.SUBJECT_MAP), null, true);
            Statement statement = statements.next();

            Resource subjectMap = (Resource) statement.getObject();
            
            extractProperties(repository, triplesMap, subjectMap);
            
            //AD: The values of the rr:class property must be IRIs. 
            //AD: Would that mean that it can not be a reference to an extract of the input or a template?
            Set<URI> classIRIs = TermExtractor.extractURIsFromTermMap(
                    repository, subjectMap, R2RMLVocabulary.R2RMLTerm.CLASS);

            if (graphMapValues != null && graphMapValues.size() > 0) {
                GraphMapExtractor graphMapExtractor = new GraphMapExtractor();
                graphMaps = graphMapExtractor.extractGraphMapValues(
                        repository, graphMapValues, savedGraphMaps, triplesMap);
                log.debug("Graph Maps returned " + graphMaps);
            }

            try {

                if (connection.hasStatement(
                        (Resource) statement.getObject(),
                        vf.createURI(CRMLVocabulary.CRML_NAMESPACE
                        + CRMLVocabulary.cRMLTerm.BOOLEAN_CONDITION), null, true)) {
                    log.debug("Condition Subject Map Extractor");
                    ConditionPredicateObjectMapExtractor preObjMapExtractor = 
                            new ConditionPredicateObjectMapExtractor();
                    conditions = preObjMapExtractor.extractConditions(
                            repository, (Resource) statement.getObject());
                    log.debug(conditions.size() + " conditions were found");
                    result = new StdConditionSubjectMap(triplesMap, constantValue, 
                            stringTemplate, termType, inverseExpression,
                            referenceValue, classIRIs, graphMaps, conditions);
                } else {
                    log.debug("Simple Subject Map Extractor");
                    result = new StdSubjectMap(triplesMap, constantValue,
                            stringTemplate, termType, inverseExpression, 
                            referenceValue, classIRIs, graphMaps);
                }
            } catch (Exception ex) {
                log.error("Exception: " + ex);
            }
            connection.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return result;
    }

}
