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

package be.ugent.mmlab.rml.extraction.concrete;

import be.ugent.mmlab.rml.model.RDFTerm.GraphMap;
import be.ugent.mmlab.rml.model.RDFTerm.ObjectMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdObjectMap;
import be.ugent.mmlab.rml.model.termMap.ReferenceMap;
import be.ugent.mmlab.rml.sesame.RMLSesameDataSet;
import be.ugent.mmlab.rml.vocabulary.R2RMLVocabulary;
import java.util.Set;
import org.apache.log4j.LogManager;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

public class ObjectMapExtractor {
    
    // Log
    private static final org.apache.log4j.Logger log = LogManager.getLogger(ObjectMapExtractor.class);
    
    public ObjectMap extractObjectMap(RMLSesameDataSet rmlMappingGraph,
            Resource object, Set<GraphMap> graphMaps, TriplesMap triplesMap){
        try {
            log.debug(
                    Thread.currentThread().getStackTrace()[1].getMethodName() + ": " 
                    + "Extract object map..");
            // Extract object maps properties
            Value constantValue = TermMapExtractor.extractValueFromTermMap(rmlMappingGraph,
                    object, R2RMLVocabulary.R2RMLTerm.CONSTANT, triplesMap);
            String stringTemplate = TermMapExtractor.extractLiteralFromTermMap(rmlMappingGraph,
                    object, R2RMLVocabulary.R2RMLTerm.TEMPLATE, triplesMap);
            String languageTag = TermMapExtractor.extractLiteralFromTermMap(rmlMappingGraph,
                    object, R2RMLVocabulary.R2RMLTerm.LANGUAGE, triplesMap);
            URI termType = (URI) TermMapExtractor.extractValueFromTermMap(rmlMappingGraph, object,
                    R2RMLVocabulary.R2RMLTerm.TERM_TYPE, triplesMap);
            URI dataType = (URI) TermMapExtractor.extractValueFromTermMap(rmlMappingGraph, object,
                    R2RMLVocabulary.R2RMLTerm.DATATYPE, triplesMap);
            String inverseExpression = TermMapExtractor.extractLiteralFromTermMap(rmlMappingGraph,
                    object, R2RMLVocabulary.R2RMLTerm.INVERSE_EXPRESSION, triplesMap);
            TermMapExtractor termMapExtractor = new TermMapExtractor();
            //MVS: Decide on ReferenceIdentifier
            ReferenceMap referenceValue = 
                    termMapExtractor.extractReferenceIdentifier(rmlMappingGraph, object, triplesMap);
            //TODO:add the following validator
            //validator.checkTermMap(constantValue, stringTemplate, referenceValue, o.stringValue());

            StdObjectMap result = new StdObjectMap(null, constantValue, dataType,
                    languageTag, stringTemplate, termType, inverseExpression,
                    referenceValue);// split, process, replace,
                    //equalCondition, processCondition, splitCondition, bindCondition);

            return result;
        } catch (Exception ex) {
            log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + ex);
        } 
        return null;
    }

}
