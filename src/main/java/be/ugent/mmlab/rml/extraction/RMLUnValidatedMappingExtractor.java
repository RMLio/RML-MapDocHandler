package be.ugent.mmlab.rml.extraction;

import be.ugent.mmlab.rml.model.std.StdTriplesMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.sesame.RMLSesameDataSet;
import be.ugent.mmlab.rml.vocabulary.R2RMLVocabulary;
import be.ugent.mmlab.rml.vocabulary.RMLVocabulary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * RML - Mapping Document Handler
 *
 * @author andimou
 */
public class RMLUnValidatedMappingExtractor implements RMLMappingExtractor{
    
    // Log
    private static final Logger log = LogManager.getLogger(RMLUnValidatedMappingExtractor.class);
    // Value factory
    private static ValueFactory vf = new ValueFactoryImpl();
    
    /**
     * Constant-valued term maps can be expressed more concisely using the
     * constant shortcut properties rr:subject, rr:predicate, rr:object and
     * rr:graph. Occurrences of these properties must be treated exactly as if
     * the following triples were present in the mapping graph instead.
     *
     * @param rmlMappingGraph
     */
    @Override
    public RMLSesameDataSet replaceShortcuts(RMLSesameDataSet rmlMappingGraph) {
        Map<URI, URI> shortcutPredicates = new HashMap<URI, URI>();
        shortcutPredicates.put(
                vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.SUBJECT),
                vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.SUBJECT_MAP));
        shortcutPredicates.put(
                vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.PREDICATE),
                vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.PREDICATE_MAP));
        shortcutPredicates.put(vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.OBJECT), 
                vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.OBJECT_MAP));
        shortcutPredicates
                .put(vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.GRAPH),
                vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.GRAPH_MAP));
        
        for (URI u : shortcutPredicates.keySet()) {
            List<Statement> shortcutTriples = rmlMappingGraph.tuplePattern(
                    null, u, null);
            log.debug(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "Number of RML shortcuts found "
                    + "for "
                    + u.getLocalName()
                    + " : "
                    + shortcutTriples.size());
            
            for (Statement shortcutTriple : shortcutTriples) {
                rmlMappingGraph.remove(shortcutTriple.getSubject(),
                        shortcutTriple.getPredicate(),
                        shortcutTriple.getObject());
                BNode blankMap = vf.createBNode();

                URI pMap = vf.createURI(shortcutPredicates.get(u).toString());
                URI pConstant = vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                        + R2RMLVocabulary.R2RMLTerm.CONSTANT);
                rmlMappingGraph.add(shortcutTriple.getSubject(), pMap,
                        blankMap);
                rmlMappingGraph.add(blankMap, pConstant,
                        shortcutTriple.getObject());
            }
        }

        return rmlMappingGraph;
    }
    
    /**
     *
     * @param rmlMappingGraph
     */
    @Override
    public RMLSesameDataSet skolemizeStatements(RMLSesameDataSet rmlMappingGraph) {
        Map<URI, URI> predicates = new HashMap<URI, URI>();
        predicates.put(
                vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.SUBJECT_MAP),
                vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.SUBJECT_MAP));
        predicates.put(
                vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.PREDICATE_MAP),
                vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.PREDICATE_MAP));
        predicates.put(vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.OBJECT_MAP),
                vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.OBJECT_MAP));
        predicates.put(vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.PARENT_TRIPLES_MAP),
                vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.PARENT_TRIPLES_MAP));
        predicates.put(vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.JOIN_CONDITION),
                vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.JOIN_CONDITION));
        predicates
                .put(vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.GRAPH_MAP),
                vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.GRAPH_MAP));
        predicates
                .put(vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.CLASS),
                vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.CLASS));
        predicates
                .put(vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.CONSTANT),
                vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.CONSTANT));

        predicates
                .put(vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.TEMPLATE),
                vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.TEMPLATE));
        predicates
                .put(vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.TERM_TYPE),
                vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.TERM_TYPE));

        predicates
                .put(vf.createURI(RMLVocabulary.RML_NAMESPACE
                + RMLVocabulary.RMLTerm.LOGICAL_SOURCE),
                vf.createURI(RMLVocabulary.RML_NAMESPACE
                + RMLVocabulary.RMLTerm.LOGICAL_SOURCE));
        predicates
                .put(vf.createURI(RMLVocabulary.RML_NAMESPACE
                + RMLVocabulary.RMLTerm.SOURCE),
                vf.createURI(RMLVocabulary.RML_NAMESPACE
                + RMLVocabulary.RMLTerm.SOURCE));
        predicates
                .put(vf.createURI(RMLVocabulary.RML_NAMESPACE
                + RMLVocabulary.RMLTerm.REFERENCE_FORMULATION),
                vf.createURI(RMLVocabulary.RML_NAMESPACE
                + RMLVocabulary.RMLTerm.REFERENCE_FORMULATION));
        predicates
                .put(vf.createURI(RMLVocabulary.RML_NAMESPACE
                + RMLVocabulary.RMLTerm.REFERENCE),
                vf.createURI(RMLVocabulary.RML_NAMESPACE
                + RMLVocabulary.RMLTerm.REFERENCE));
        predicates
                .put(vf.createURI(RMLVocabulary.RML_NAMESPACE
                + RMLVocabulary.RMLTerm.ITERATOR),
                vf.createURI(RMLVocabulary.RML_NAMESPACE
                + RMLVocabulary.RMLTerm.ITERATOR));
        
        predicates
                .put(vf.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                + "type"),
                vf.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                + "type"));

        for (URI u : predicates.keySet()) {
            List<Statement> triples = rmlMappingGraph.tuplePattern(
                    null, u, null);
            log.debug(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "Number of statements found "
                    + "for "
                    + u.getLocalName()
                    + " : "
                    + triples.size());
        }
        return rmlMappingGraph;
    }
       
    /**
     * Construct TriplesMap objects rule. A triples map is represented by a
     * resource that references the following other resources : - It must have
     * exactly one subject map * using the rr:subjectMap property.
     *
     * @param rmlMappingGraph
     * @return
     */
    @Override
    public Map<Resource, TriplesMap> extractTriplesMapResources(
            RMLSesameDataSet rmlMappingGraph) {
        Map<Resource, TriplesMap> triplesMapResources = new HashMap<Resource, TriplesMap>();
        
        List<Statement> statements = getTriplesMapResources(rmlMappingGraph);

        triplesMapResources = putTriplesMapResources(statements, triplesMapResources);

        return triplesMapResources;
    }
    
    /**
     *
     * @param rmlMappingGraph
     * @return
     */
    protected List<Statement> getTriplesMapResources(RMLSesameDataSet rmlMappingGraph){
        
        URI o = rmlMappingGraph.URIref(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.TRIPLES_MAP_CLASS);
        URI p = rmlMappingGraph.URIref("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        List<Statement> statements = rmlMappingGraph.tuplePattern(null, p, o);
        return statements;
    }
    
    /**
     *
     * @param statements
     * @param triplesMapResources
     * @return
     */
    protected Map<Resource, TriplesMap> putTriplesMapResources(
            List<Statement> statements, Map<Resource, TriplesMap> triplesMapResources) {
        for (Statement s : statements) {
            //try {
                triplesMapResources.put(s.getSubject(),
                        new StdTriplesMap(null, null, null, s.getSubject().stringValue()));
            /*}  catch (InvalidR2RMLStructureException ex) {
                java.util.logging.Logger.getLogger(RMLUnValidatedMappingExtractor.class.getName()).log(Level.SEVERE, null, ex);
            }*/
        }
        return triplesMapResources;
    }
}
