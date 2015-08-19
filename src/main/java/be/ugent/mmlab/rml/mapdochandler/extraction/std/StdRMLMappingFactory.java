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
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
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
        this.extractor = new StdRMLMappingExtractor();
    }
    
    public RMLMapping extractRMLMapping(Repository mapDocRepo) {
        RMLMapping result = null;
        try {
            RepositoryConnection mapDocRepoCon = mapDocRepo.getConnection();
            log.debug("Number of RML triples: " + mapDocRepoCon.size());
            
            // Transform RDF with replacement shortcuts
            log.info("Replacing Mapping Document shortcuts..");
            mapDocRepo = extractor.replaceShortcuts(mapDocRepo);
            log.debug("Number of RML triples after expanding shortcuts: "
                    + mapDocRepoCon.size());
            mapDocRepoCon.close();
            
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

        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return result;
    }
    
    /*private static void launchPreChecks(RMLSesameDataSet rmlMappingGraph) {
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
    }*/

}
