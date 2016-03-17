package be.ugent.mmlab.rml.mapdochandler.extraction.concrete;

import be.ugent.mmlab.rml.model.RDFTerm.GraphMap;
import be.ugent.mmlab.rml.model.RDFTerm.ObjectMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdConditionObjectMap;
import be.ugent.mmlab.rml.model.std.StdObjectMap;
import be.ugent.mmlab.rml.vocabularies.R2RMLVocabulary;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;

/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : ObjectMapExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */

public class ObjectMapExtractor extends StdTermMapExtractor {
    
    // Log
    static final Logger log = LoggerFactory.getLogger(
            ObjectMapExtractor.class.getSimpleName());
    
    public ObjectMap extractObjectMap(Repository repository,
            Resource object, Set<GraphMap> graphMaps, TriplesMap triplesMap){
        ObjectMap result ;
        log.debug("Extracting Object Map..");
        
        try {
            extractProperties(repository, triplesMap, object);

            //Extract additional properties for Object Map
            String languageTag = TermExtractor.extractLiteralFromTermMap(repository,
                    object, R2RMLVocabulary.R2RMLTerm.LANGUAGE, triplesMap);
            URI dataType = (URI) TermExtractor.extractValueFromTermMap(repository, object,
                    R2RMLVocabulary.R2RMLTerm.DATATYPE, triplesMap);
            
            if(conditions != null && conditions.size() > 0){
                log.debug("Conditional Object Map extracted.");
                result = new StdConditionObjectMap(triplesMap, null, 
                    constantValue, dataType, languageTag, stringTemplate, 
                    termType, inverseExpression, referenceValue, conditions);
            }
            else{
                log.debug("Simple Object Map extracted.");
                result = new StdObjectMap(triplesMap, null, 
                    constantValue, dataType, languageTag, stringTemplate, 
                    termType, inverseExpression, referenceValue);
            }

            return result;
        } catch (Exception ex) {
            log.error("Exception: " + ex);
        } 
        return null;
    }

}
