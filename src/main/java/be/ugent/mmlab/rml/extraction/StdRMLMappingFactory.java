package be.ugent.mmlab.rml.extraction;

import be.ugent.mmlab.rml.extraction.concrete.TriplesMapExtractor;
import be.ugent.mmlab.rml.model.RMLMapping;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.retrieval.RMLDocRetrieval;
import be.ugent.mmlab.rml.sesame.RMLSesameDataSet;
import be.ugent.mmlab.rml.vocabulary.R2RMLVocabulary;
import be.ugent.mmlab.rml.vocabulary.Vocab;
import java.util.List;
import java.util.Map;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RML Mapping Document Handler : StdRMLMappingFactory
 *
 * @author andimou
 */
public class StdRMLMappingFactory {
    
    // Log
    private static final Logger log = LoggerFactory.getLogger(RMLMappingFactory.class);
    
    private RMLMappingExtractor extractor;
    
    public StdRMLMappingFactory(){
        this.extractor = new RMLUnValidatedMappingExtractor();
    }
    
    public RMLMapping extractRMLMapping(String fileToRMLFile){
            //throws RepositoryException, RDFParseException, IOException, Exception {
        RMLSesameDataSet rmlMappingGraph ;
        
        //Retrieve the Mapping Document
        RMLDocRetrieval mapDocRetrieval = new RMLDocRetrieval() ;        
        rmlMappingGraph = mapDocRetrieval.getMappingDoc(fileToRMLFile, RDFFormat.TURTLE);

        log.debug(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                + "Number of RML triples in file "
                + fileToRMLFile + " : " + rmlMappingGraph.getSize());        
        // Transform RDF with replacement shortcuts
        rmlMappingGraph = extractor.replaceShortcuts(rmlMappingGraph);
        // Run few tests to help user in its RDF syntax
        launchPreChecks(rmlMappingGraph);
               
        // Construct RML Mapping object
        //TODO:Disambiguate which extractor (from which project to be used)
        Map<Resource, TriplesMap> triplesMapResources = 
                extractor.extractTriplesMapResources(rmlMappingGraph);
               
        log.debug(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                + "Number of RML triples with "
                + " type "
                + Vocab.R2RMLTerm.TRIPLES_MAP_CLASS
                + " in file "
                + fileToRMLFile + " : " + triplesMapResources.size());
        // Fill each triplesMap object
        for (Resource triplesMapResource : triplesMapResources.keySet()) // Extract each triplesMap
        {
            TriplesMapExtractor triplesMapExtractor = new TriplesMapExtractor();
            triplesMapExtractor.extractTriplesMap(rmlMappingGraph, triplesMapResource,
                    triplesMapResources);
        }
        // Generate RMLMapping object
        RMLMapping result = new RMLMapping(triplesMapResources.values());
        return result;
    }
    
    private static void launchPreChecks(RMLSesameDataSet rmlMappingGraph) {
        // Pre-check 1 : test if a triplesMap with predicateObject map exists
        // without subject map
        URI p = rmlMappingGraph.URIref(R2RMLVocabulary.R2RML_NAMESPACE
                + Vocab.R2RMLTerm.PREDICATE_OBJECT_MAP);
        List<Statement> statements = rmlMappingGraph.tuplePattern(null, p,
                null);
        for (Statement s : statements) {
            p = rmlMappingGraph.URIref(R2RMLVocabulary.R2RML_NAMESPACE
                    + Vocab.R2RMLTerm.SUBJECT_MAP);
            List<Statement> otherStatements = rmlMappingGraph.tuplePattern(
                    s.getSubject(), p, null);
            if (otherStatements.isEmpty()) {
                log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                        + "You have a triples map without subject map : "
                        + s.getSubject().stringValue() + ".");
            }
        }
    }

}
