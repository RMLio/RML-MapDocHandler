package be.ugent.mmlab.rml.mapdochandler.extraction.std;

import be.ugent.mmlab.rml.mapdochandler.extraction.RMLMappingExtractor;
import be.ugent.mmlab.rml.mapdochandler.extraction.RMLMappingFactory;
import be.ugent.mmlab.rml.mapdochandler.extraction.concrete.TriplesMapExtractor;
import be.ugent.mmlab.rml.model.RMLMapping;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.vocabularies.R2RMLVocabulary;
import java.util.Map;
import org.openrdf.model.Resource;
import org.openrdf.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : StdRMLMappingFactory
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */
public class StdRMLMappingFactory {
    
    // Log
    private static final Logger log = 
            LoggerFactory.getLogger(
            RMLMappingFactory.class.getSimpleName());
    
    private RMLMappingExtractor extractor;
    private boolean skolemization = false;
    
    public StdRMLMappingFactory(){
        this.extractor = new StdRMLMappingExtractor();
    }
    
    public StdRMLMappingFactory(boolean skolemization){
        this.extractor = new StdRMLMappingExtractor(skolemization);
        this.skolemization = skolemization;
    }
    
    public Repository prepareExtractRMLMapping(Repository mapDocRepo) {
        // Transform RML with replacement shortcuts
        log.info("Replacing Mapping Document shortcuts..");
        mapDocRepo = extractor.replaceShortcuts(mapDocRepo);

        //Transform RML with skolemized Blank Nodes
        if (skolemization) {
            mapDocRepo = extractor.skolemizeStatements(mapDocRepo);
        }
        return mapDocRepo;
    }
    
    public RMLMapping extractRMLMapping(Repository mapDocRepo) {
        RMLMapping result = null;

        mapDocRepo = prepareExtractRMLMapping(mapDocRepo);
        // Run few tests to help user in its RDF syntax
        //launchPreChecks(rmlMappingGraph);

        // Construct RML Mapping object
        //TODO:Disambiguate which extractor (from which project to be used)
        log.info("Extracting Triples Maps..");
        Map<Resource, TriplesMap> triplesMapResources =
                extractor.extractTriplesMapResources(mapDocRepo);
        log.debug("Number of RML triples with type "
                + R2RMLVocabulary.R2RMLTerm.TRIPLES_MAP_CLASS
                + " in file: " + triplesMapResources.size());

        // Fill each triplesMap object
        for (Resource triplesMapResource : triplesMapResources.keySet()) // Extract each triplesMap
        {
            TriplesMapExtractor triplesMapExtractor = new TriplesMapExtractor();
            triplesMapExtractor.extractTriplesMap(mapDocRepo, triplesMapResource,
                    triplesMapResources);
        }
        // Generate RMLMapping object
        result = new RMLMapping(triplesMapResources.values());
        return result;
    }
}
