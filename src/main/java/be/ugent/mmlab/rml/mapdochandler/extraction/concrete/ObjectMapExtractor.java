package be.ugent.mmlab.rml.mapdochandler.extraction.concrete;

import be.ugent.mmlab.rml.model.RDFTerm.GraphMap;
import be.ugent.mmlab.rml.model.RDFTerm.LanguageMap;
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
            log.debug("Extracting the Object Map..");
            // Extract object maps properties
            Value constantValue = TermMapExtractor.
                    extractValueFromTermMap(repository, object, 
                    R2RMLVocabulary.R2RMLTerm.CONSTANT, triplesMap);
            String stringTemplate = TermMapExtractor.
                    extractLiteralFromTermMap(repository, object, 
                    R2RMLVocabulary.R2RMLTerm.TEMPLATE, triplesMap);
            ReferenceMap referenceValue = TermMapExtractor.
                    extractReferenceIdentifier(repository, object, triplesMap);
            
            LanguageMap languageMap = LanguageMapExtractor.extractLanguageMap(
                    repository, object, triplesMap);

            URI termType = (URI) TermMapExtractor.
                    extractValueFromTermMap(repository, object,
                    R2RMLVocabulary.R2RMLTerm.TERM_TYPE, triplesMap);
            URI dataType = (URI) TermMapExtractor.
                    extractValueFromTermMap(repository, object,
                    R2RMLVocabulary.R2RMLTerm.DATATYPE, triplesMap);
            String inverseExpression = TermMapExtractor.
                    extractLiteralFromTermMap(repository, object, 
                    R2RMLVocabulary.R2RMLTerm.INVERSE_EXPRESSION, triplesMap);
            
            //TODO:add the following validator
            //validator.checkTermMap(constantValue, stringTemplate, referenceValue, o.stringValue());

            StdObjectMap result = new StdObjectMap(null, constantValue, dataType,
                    languageMap, stringTemplate, termType, inverseExpression,
                    referenceValue);
            return result;
        } catch (Exception ex) {
            log.error("Exception: " + ex);
        } 
        return null;
    }

}
