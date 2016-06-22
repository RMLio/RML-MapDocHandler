package be.ugent.mmlab.rml.mapdochandler.extraction.concrete;

import be.ugent.mmlab.rml.mapdochandler.extraction.condition.ConditionPredicateObjectMapExtractor;
import be.ugent.mmlab.rml.model.RDFTerm.GraphMap;
import be.ugent.mmlab.rml.model.RDFTerm.PredicateMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdConditionPredicateMap;
import be.ugent.mmlab.rml.model.std.StdPredicateMap;
import be.ugent.mmlab.rml.vocabularies.CRMLVocabulary;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : PredicateMapExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */
public class PredicateMapExtractor extends StdTermMapExtractor {
    
    // Log
    static final Logger log = 
            LoggerFactory.getLogger(
            PredicateMapExtractor.class.getSimpleName());
    
    public PredicateMap extractPredicateMap(
            Repository repository, Statement statement,
            GraphMap graphMap, TriplesMap triplesMap) {
        Resource object = (Resource) statement.getObject();
        PredicateMap result;
        log.debug("Extracting Predicate Map..");
        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();
            
            extractProperties(repository, triplesMap, object);

            graphMap = extractGraphMap(repository, triplesMap, graphMap);
            if (graphMap != null)
                log.debug("Found Graph Map for this Predicate Map " + graphMap.getConstantValue());
            
            if (connection.hasStatement(
                    object, vf.createURI(CRMLVocabulary.CRML_NAMESPACE
                    + CRMLVocabulary.cRMLTerm.BOOLEAN_CONDITION), null, true)) {
                log.debug("Conditional Predicate Map extracted");
                ConditionPredicateObjectMapExtractor preObjMapExtractor =
                        new ConditionPredicateObjectMapExtractor();
                conditions = preObjMapExtractor.extractConditions(
                        repository, object);
                result = new StdConditionPredicateMap(triplesMap, null,
                    constantValue, stringTemplate, inverseExpression, 
                        referenceValue, termType, conditions, graphMap);
            } else {
                log.debug("Simple Predicate Map extracted");
                result = new StdPredicateMap(triplesMap, null, constantValue, 
                        stringTemplate, inverseExpression, referenceValue, termType, graphMap);
            }
            connection.close();
            return result;
        } catch (Exception ex) {
            log.error("Exception: " + ex);
        } 
        return null;
    }

}
