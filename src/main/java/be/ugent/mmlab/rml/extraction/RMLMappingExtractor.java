package be.ugent.mmlab.rml.extraction;

import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.sesame.RMLSesameDataSet;
import java.util.Map;
import org.openrdf.model.Resource;

/**
 * RML - Mapping Document Handler
 *
 * @author andimou
 */
public interface RMLMappingExtractor {
    
    /**
     *
     * @param rmlMappingGraph
     */
    public RMLSesameDataSet replaceShortcuts(RMLSesameDataSet rmlMappingGraph);
    
    /**
     *
     * @param rmlMappingGraph
     * @return
     */
    public RMLSesameDataSet skolemizeStatements(RMLSesameDataSet rmlMappingGraph);
    
    /**
     *
     * @param rmlMappingGraph
     * @return
     */
    public Map<Resource, TriplesMap> extractTriplesMapResources(
            RMLSesameDataSet rmlMappingGraph);
    
    /**
     *
     * @param rmlMappingGraph
     * @param triplesMapSubject
     * @param triplesMapResources
     */
    //public void extractTriplesMap(
    //        RMLSesameDataSet rmlMappingGraph, Resource triplesMapSubject, 
    //        Map<Resource, TriplesMap> triplesMapResources);
    
    /**
     *
     * @param rmlMappingGraph
     * @param object
     * @param graphMaps
     * @param triplesMap
     * @return
     */
    //public PredicateMap extractPredicateMap(
    //        RMLSesameDataSet rmlMappingGraph, Statement object,
    //        Set<GraphMap> graphMaps, TriplesMap triplesMap);
    
    /**
     *
     * @param rmlMappingGraph
     * @param object
     * @param graphMaps
     * @param triplesMap
     * @return
     */
    //public ObjectMap extractObjectMap(RMLSesameDataSet rmlMappingGraph,
    //        Resource object, Set<GraphMap> graphMaps, TriplesMap triplesMap);
    
    /**
     *
     * @param rmlMappingGraph
     * @param triplesMapSubject
     * @param graphMaps
     * @param result
     * @param triplesMapResources
     * @return
     */
    //public Set<PredicateObjectMap> extractPredicateObjectMaps(
    //        RMLSesameDataSet rmlMappingGraph, Resource triplesMapSubject,
    //        Set<GraphMap> graphMaps, TriplesMap result,
    //        Map<Resource, TriplesMap> triplesMapResources);
        
}
