package be.ugent.mmlab.rml.extraction.concrete;

import be.ugent.mmlab.rml.model.RDFTerm.GraphMap;
import be.ugent.mmlab.rml.model.RDFTerm.SubjectMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdSubjectMap;
import be.ugent.mmlab.rml.model.termMap.ReferenceMap;
import be.ugent.mmlab.rml.sesame.RMLSesameDataSet;
import be.ugent.mmlab.rml.vocabulary.R2RMLVocabulary;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

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
public class SubjectMapExtractor {
    
    // Log
    static final Logger log = LoggerFactory.getLogger(SubjectMapExtractor.class);
    
    public SubjectMap extractSubjectMap(
            RMLSesameDataSet rmlMappingGraph, Resource triplesMapSubject,
            Set<GraphMap> savedGraphMaps, TriplesMap triplesMap){
        log.debug(
                Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                + "Extract subject map...");
        
        // Extract subject map
        List<Statement> statements = rmlMappingGraph.tuplePattern(triplesMapSubject,
                RMLTermExtractor.getTermURI(rmlMappingGraph, R2RMLVocabulary.R2RMLTerm.SUBJECT_MAP), null);
        
        Resource subjectMap ; 

        if(statements != null && statements.size() > 0)
            subjectMap = (Resource) statements.get(0).getObject();
        else
            return null;
        
        log.debug(
                Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                + "Found subject map : "
                + subjectMap.stringValue());

        Value constantValue = TermMapExtractor.extractValueFromTermMap(rmlMappingGraph,
                subjectMap, R2RMLVocabulary.R2RMLTerm.CONSTANT, triplesMap);
        
        String stringTemplate = TermMapExtractor.extractLiteralFromTermMap(rmlMappingGraph,
                subjectMap, R2RMLVocabulary.R2RMLTerm.TEMPLATE, triplesMap);
        
        URI termType = (URI) TermMapExtractor.extractValueFromTermMap(rmlMappingGraph,
                subjectMap, R2RMLVocabulary.R2RMLTerm.TERM_TYPE, triplesMap);
        String inverseExpression = TermMapExtractor.extractLiteralFromTermMap(rmlMappingGraph,
                subjectMap, R2RMLVocabulary.R2RMLTerm.INVERSE_EXPRESSION, triplesMap);
        //TODO:fix the following validation
        //validator.checkTermMap(constantValue, stringTemplate, null, subjectMap.toString());

        //TODO:Add check if the referenceValue is a valid reference according to the reference formulation
        TermMapExtractor termMapExtractor = new TermMapExtractor();
        ReferenceMap referenceValue = 
                termMapExtractor.extractReferenceIdentifier(rmlMappingGraph, subjectMap, triplesMap);
        
        //AD: The values of the rr:class property must be IRIs. 
        //AD: Would that mean that it can not be a reference to an extract of the input or a template?
        Set<URI> classIRIs = TermMapExtractor.extractURIsFromTermMap(rmlMappingGraph,
                subjectMap, R2RMLVocabulary.R2RMLTerm.CLASS);
        
        //AD:Move it a separate function that extracts the GraphMaps
        Set<GraphMap> graphMaps = new HashSet<GraphMap>();
        Set<Value> graphMapValues = TermMapExtractor.extractValuesFromResource(
                rmlMappingGraph, subjectMap, R2RMLVocabulary.R2RMLTerm.GRAPH_MAP);
       
        if (graphMapValues != null) {
            GraphMapExtractor graphMapExtractor = new GraphMapExtractor();
            graphMaps = graphMapExtractor.extractGraphMapValues(
                    rmlMappingGraph, graphMapValues, savedGraphMaps, triplesMap);
            log.info(
                    Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "graph Maps returned " + graphMaps);
        }
        
        SubjectMap result = null;
        try {
            result = new StdSubjectMap(triplesMap, constantValue,
                    stringTemplate, termType, inverseExpression,
                    referenceValue, classIRIs, graphMaps);
        } catch (Exception ex) {
            log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + ex);
        }
        log.debug(
                Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                + "Subject map extracted.");
        return result;
    }

}
