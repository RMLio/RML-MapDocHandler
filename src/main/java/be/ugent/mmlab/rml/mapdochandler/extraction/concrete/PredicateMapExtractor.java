package be.ugent.mmlab.rml.mapdochandler.extraction.concrete;

import be.ugent.mmlab.rml.model.RDFTerm.GraphMap;
import be.ugent.mmlab.rml.model.RDFTerm.PredicateMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdConditionPredicateMap;
import be.ugent.mmlab.rml.model.std.StdPredicateMap;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;

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
            Set<GraphMap> graphMaps, TriplesMap triplesMap) {
        Resource object = (Resource) statement.getObject();
        PredicateMap result;
        log.debug("Extracting Predicate Map..");
        try {
            extractProperties(repository, triplesMap, object);
            
            if(conditions != null && conditions.size() > 0){
                log.debug("Conditional Predicate Map extracted");
                result = new StdConditionPredicateMap(triplesMap, null, 
                    constantValue, stringTemplate, inverseExpression, 
                        referenceValue, termType, conditions);
            }
            else{
                log.debug("Simple Predicate Map extracted");
                result = new StdPredicateMap(triplesMap, null, constantValue, 
                        stringTemplate, inverseExpression, referenceValue, termType);
            }

            return result;
        } catch (Exception ex) {
            log.error("Exception: " + ex);
        }
        return null;
    }

}
