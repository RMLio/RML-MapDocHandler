package be.ugent.mmlab.rml.extraction;

import be.ugent.mmlab.rml.sesame.RMLSesameDataSet;
import be.ugent.mmlab.rml.skolemization.skolemizationFactory;
import be.ugent.mmlab.rml.vocabulary.R2RMLVocabulary;
import be.ugent.mmlab.rml.vocabulary.RMLVocabulary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : StdRMLMappingExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */
public abstract class StdRMLMappingExtractor implements RMLMappingExtractor{
    
    // Log
    private static final Logger log = LoggerFactory.getLogger(StdRMLMappingExtractor.class);
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
        
        for (URI u : predicates.keySet()) {
            List<Statement> triples = rmlMappingGraph.tuplePattern(
                    null, u, null);
            log.debug(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "Number of statements found "
                    + "for "
                    + u.getLocalName()
                    + " : "
                    + triples.size());

            for (Statement triple : triples) {
                Resource blankSubjectMap = triple.getSubject();
                Resource skolemizedMap = skolemizationFactory.skolemizeBlankNode(blankSubjectMap);
                if (triple.getSubject().toString().startsWith("_:")) {
                    skolemizationFactory.skolemSubstitution(triple.getSubject(), skolemizedMap, rmlMappingGraph);
                }
            }
            for (Statement triple : triples) {
                Value blankObjectMap = triple.getObject();
                Resource skolemizedMap = skolemizationFactory.skolemizeBlankNode(blankObjectMap);
                if (triple.getObject().toString().startsWith("_:") && 
                        (  triple.getObject().getClass() == Resource.class
                        || triple.getObject().getClass() == Value.class
                        || triple.getObject().getClass() == org.openrdf.sail.memory.model.MemBNode.class) ) {
                    skolemizationFactory.skolemSubstitution(triple.getObject(), skolemizedMap, rmlMappingGraph);
                }

            }
        }
        return rmlMappingGraph;
    }
    
    

}
