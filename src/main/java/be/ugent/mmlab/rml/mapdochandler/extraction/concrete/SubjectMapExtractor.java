package be.ugent.mmlab.rml.mapdochandler.extraction.concrete;

import be.ugent.mmlab.rml.extraction.RMLTermExtractor;
import be.ugent.mmlab.rml.extraction.TermExtractor;
import be.ugent.mmlab.rml.mapdochandler.extraction.condition.ConditionPredicateObjectMapExtractor;
import be.ugent.mmlab.rml.model.RDFTerm.FunctionTermMap;
import be.ugent.mmlab.rml.model.RDFTerm.GraphMap;
import be.ugent.mmlab.rml.model.RDFTerm.SubjectMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdConditionSubjectMap;
import be.ugent.mmlab.rml.model.std.StdSubjectMap;
import be.ugent.mmlab.rml.vocabularies.CRMLVocabulary;
import be.ugent.mmlab.rml.vocabularies.R2RMLVocabulary;

import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;

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
            GraphMap graphMap, TriplesMap triplesMap,
            Map<Resource, TriplesMap> triplesMapResources) {
        SubjectMap result = null;
        RepositoryResult<Statement> statements;
        FunctionTermMap funSubjectMap = null;
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
            
            Set<IRI> classIRIs = TermExtractor.extractURIsFromTermMap(
                    repository, subjectMap, R2RMLVocabulary.R2RMLTerm.CLASS);

            graphMap = extractGraphMap(repository, triplesMap, graphMap);

            try {

                if (connection.hasStatement(
                        (Resource) statement.getObject(),
                        vf.createIRI(CRMLVocabulary.CRML_NAMESPACE
                        + CRMLVocabulary.cRMLTerm.BOOLEAN_CONDITION), null, true)) {
                    log.debug("Condition Subject Map Extractor");
                    ConditionPredicateObjectMapExtractor preObjMapExtractor = 
                            new ConditionPredicateObjectMapExtractor();
                    conditions = preObjMapExtractor.extractConditions(
                            repository, (Resource) statement.getObject(), null, triplesMap);
                    log.debug(conditions.size() + " conditions were found");
                    result = new StdConditionSubjectMap(triplesMap, constantValue, 
                            stringTemplate, termType, inverseExpression,
                            referenceValue, classIRIs, graphMap, conditions);
                } else {
                    FunctionTermMapExtractor funSubjMapExtractor =
                            new FunctionTermMapExtractor();
                    Set<FunctionTermMap> fnMaps = funSubjMapExtractor.processFunctionTermMap(
                            repository, subjectMap,
                            triplesMapResources, triplesMap, null, graphMap);

                    if (fnMaps.size() > 0) {
                        funSubjectMap = (FunctionTermMap) fnMaps.toArray()[0];
                    }

                    log.debug("Simple Subject Map Extractor");
                    result = new StdSubjectMap(triplesMap, constantValue,
                            stringTemplate, termType, inverseExpression,
                            referenceValue, classIRIs, graphMap, funSubjectMap);
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
