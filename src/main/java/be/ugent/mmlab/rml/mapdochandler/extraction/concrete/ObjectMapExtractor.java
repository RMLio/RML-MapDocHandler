package be.ugent.mmlab.rml.mapdochandler.extraction.concrete;

import be.ugent.mmlab.rml.model.RDFTerm.GraphMap;
import be.ugent.mmlab.rml.model.RDFTerm.ObjectMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdObjectMap;
import be.ugent.mmlab.rml.model.termMap.ReferenceMap;
import be.ugent.mmlab.rml.vocabularies.R2RMLVocabulary;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
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

public class ObjectMapExtractor {
    
    // Log
    static final Logger log = LoggerFactory.getLogger(ObjectMapExtractor.class);
    
    public ObjectMap extractObjectMap(Repository repository,
            Resource object, Set<GraphMap> graphMaps, TriplesMap triplesMap){
        try {
            log.debug("Extract object map..");
            // Extract object maps properties
            Value constantValue = TermMapExtractor.extractValueFromTermMap(repository,
                    object, R2RMLVocabulary.R2RMLTerm.CONSTANT, triplesMap);
            String stringTemplate = TermMapExtractor.extractLiteralFromTermMap(repository,
                    object, R2RMLVocabulary.R2RMLTerm.TEMPLATE, triplesMap);
            String languageTag = TermMapExtractor.extractLiteralFromTermMap(repository,
                    object, R2RMLVocabulary.R2RMLTerm.LANGUAGE, triplesMap);
            URI termType = (URI) TermMapExtractor.extractValueFromTermMap(repository, object,
                    R2RMLVocabulary.R2RMLTerm.TERM_TYPE, triplesMap);
            URI dataType = (URI) TermMapExtractor.extractValueFromTermMap(repository, object,
                    R2RMLVocabulary.R2RMLTerm.DATATYPE, triplesMap);
            String inverseExpression = TermMapExtractor.extractLiteralFromTermMap(repository,
                    object, R2RMLVocabulary.R2RMLTerm.INVERSE_EXPRESSION, triplesMap);
            TermMapExtractor termMapExtractor = new TermMapExtractor();
            //MVS: Decide on ReferenceIdentifier
            ReferenceMap referenceValue = 
                    termMapExtractor.extractReferenceIdentifier(repository, object, triplesMap);
            log.debug("reference value " + referenceValue);
            //TODO:add the following validator
            //validator.checkTermMap(constantValue, stringTemplate, referenceValue, o.stringValue());

            StdObjectMap result = new StdObjectMap(triplesMap, null, 
                    constantValue, dataType, languageTag, stringTemplate, 
                    termType, inverseExpression, referenceValue);// split, process, replace,
                    //equalCondition, processCondition, splitCondition, bindCondition);

            return result;
        } catch (Exception ex) {
            log.error("Exception: " + ex);
        } 
        return null;
    }

}
