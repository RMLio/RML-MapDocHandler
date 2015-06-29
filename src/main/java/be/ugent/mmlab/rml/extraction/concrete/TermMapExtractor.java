/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : TermMapExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */

package be.ugent.mmlab.rml.extraction.concrete;

import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdReferenceMap;
import be.ugent.mmlab.rml.model.termMap.ReferenceMap;
import be.ugent.mmlab.rml.sesame.RMLSesameDataSet;
import be.ugent.mmlab.rml.vocabulary.R2RMLVocabulary;
import be.ugent.mmlab.rml.vocabulary.RMLVocabulary;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.LogManager;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * RML Processor
 *
 * @author andimou
 */
public class TermMapExtractor {
    
    // Log
    private static final org.apache.log4j.Logger log = LogManager.getLogger(TermMapExtractor.class);
    
    /**
     *
     * @param rmlMappingGraph
     * @param termType
     * @param term
     * @param triplesMap
     * @return
     */
    static protected Value extractValueFromTermMap(
            RMLSesameDataSet rmlMappingGraph, Resource termType,
            Enum term, TriplesMap triplesMap) {
        
        List<Statement> statements = rmlMappingGraph.tuplePattern(termType,
                RMLTermExtractor.getTermURI(rmlMappingGraph, term), null);
        
        if (statements.isEmpty()) 
            return null;
        else{
            log.debug(
                Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                + "Extracted "
                + term + " : " + statements.get(0).getObject().stringValue());
            return statements.get(0).getObject();
        }
        
    }
    
    protected static Set<Value> extractValuesFromResource(
            RMLSesameDataSet rmlMappingGraph,
            Resource termType,
            Enum term){
            
        URI p = RMLTermExtractor.getTermURI(rmlMappingGraph, term);

        List<Statement> statements = rmlMappingGraph.tuplePattern(termType,
                p, null);
        if (statements.isEmpty()) {
            return null;
        }
        Set<Value> values = new HashSet<Value>();
        for (Statement statement : statements) {
            Value value = statement.getObject();
            log.debug(
                    Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "Extracted "
                    + term + " : " + value.stringValue());
            values.add(value);
        }
        return values;
    }
    
    static protected String extractLiteralFromTermMap(
            RMLSesameDataSet rmlMappingGraph, Resource termType, Enum term, TriplesMap triplesMap){

        List<Statement> statements = rmlMappingGraph.tuplePattern(termType,
                RMLTermExtractor.getTermURI(rmlMappingGraph, term), null);
        
        if (statements.isEmpty()) 
            return null;
        else {
            String result = statements.get(0).getObject().stringValue();
            if (log.isDebugEnabled()) 
                log.debug(
                        Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                        + "Extracted "
                        + term + " : " + result);
            return result;
        }
    }
    
    
    
    protected static Set<URI> extractURIsFromTermMap(
            RMLSesameDataSet rmlMappingGraph, Resource termType,
            R2RMLVocabulary.R2RMLTerm term){
            
        URI p = RMLTermExtractor.getTermURI(rmlMappingGraph, term);

        List<Statement> statements = rmlMappingGraph.tuplePattern(termType,
                p, null);
        if (statements.isEmpty()) {
            return null;
        }
        Set<URI> uris = new HashSet<URI>();
        for (Statement statement : statements) {
            URI uri = (URI) statement.getObject();
            log.debug(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + term + " : " + uri.stringValue());
            uris.add(uri);
        }
        return uris;
    } 
    
    /**
     *
     * @param rmlMappingGraph
     * @param resource
     * @param triplesMap
     * @return
     */
    protected ReferenceMap extractReferenceIdentifier(
            RMLSesameDataSet rmlMappingGraph, Resource resource, TriplesMap triplesMap) {

        String columnValueStr = extractLiteralFromTermMap(
                rmlMappingGraph, resource, R2RMLVocabulary.R2RMLTerm.COLUMN, triplesMap);
        String referenceValueStr = extractLiteralFromTermMap(
                rmlMappingGraph, resource, RMLVocabulary.RMLTerm.REFERENCE, triplesMap);

        if (columnValueStr != null && referenceValueStr != null) {
            log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + resource
                    + " has a reference and column defined.");
        }
        
        if (columnValueStr != null) {
            ReferenceMap refMap = new StdReferenceMap(columnValueStr);
            return refMap.getReferenceValue(columnValueStr);
        }
        
        ReferenceMap refMap = new StdReferenceMap(referenceValueStr);
        
        return refMap.getReferenceValue(referenceValueStr);

    }

}
