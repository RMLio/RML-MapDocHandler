package be.ugent.mmlab.rml.mapdochandler.extraction.concrete;

import be.ugent.mmlab.rml.mapdochandler.extraction.condition.ConditionPredicateObjectMapExtractor;
import be.ugent.mmlab.rml.model.RDFTerm.GraphMap;
import be.ugent.mmlab.rml.model.RDFTerm.ObjectMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdConditionObjectMap;
import be.ugent.mmlab.rml.model.std.StdObjectMap;
import be.ugent.mmlab.rml.vocabularies.CRMLVocabulary;
import be.ugent.mmlab.rml.vocabularies.R2RMLVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

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
    static final Logger log = 
            LoggerFactory.getLogger(
            ObjectMapExtractor.class.getSimpleName());
    
    public ObjectMap extractObjectMap(Repository repository,
            Resource object, GraphMap graphMap, TriplesMap triplesMap){
        ObjectMap result ;
        log.debug("Extracting Object Map..");
        
        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();
            log.debug("object " + object.stringValue());
            extractProperties(repository, triplesMap, object);
            
            //Extract additional properties for Object Map
            String languageTag = TermExtractor.extractLiteralFromTermMap(repository,
                    object, R2RMLVocabulary.R2RMLTerm.LANGUAGE, triplesMap);
            URI dataType = (URI) TermExtractor.extractValueFromTermMap(repository, object,
                    R2RMLVocabulary.R2RMLTerm.DATATYPE, triplesMap);

            graphMap = extractGraphMap(repository, triplesMap, graphMap);
            if (graphMap != null)
                log.debug("Found Graph Map for this Object Map " + graphMap.getConstantValue());

            log.debug("Extracting conditions...");
            if (connection.hasStatement(
                    object, vf.createURI(CRMLVocabulary.CRML_NAMESPACE
                    + CRMLVocabulary.cRMLTerm.BOOLEAN_CONDITION), null, true)) {
                log.debug("Conditional Object Map extracted.");
                ConditionPredicateObjectMapExtractor preObjMapExtractor =
                        new ConditionPredicateObjectMapExtractor();
                conditions = preObjMapExtractor.extractConditions(
                        repository, object);
                if (conditions != null)
                    log.debug(conditions.size() + " conditions were found");
                result = new StdConditionObjectMap(triplesMap, null,
                    constantValue, dataType, languageTag, stringTemplate, 
                    termType, inverseExpression, referenceValue, conditions, graphMap);
            } else {
                log.debug("Simple Object Map extracted.");
                result = new StdObjectMap(triplesMap, null, 
                    constantValue, dataType, languageTag, stringTemplate, 
                    termType, inverseExpression, referenceValue, graphMap);
            }
            connection.close();
            return result;
        } catch (Exception ex) {
            log.error("Exception: " + ex);
        } 
        return null;
    }

}
