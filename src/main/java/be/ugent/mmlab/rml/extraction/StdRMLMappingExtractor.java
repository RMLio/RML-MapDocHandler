package be.ugent.mmlab.rml.extraction;

import be.ugent.mmlab.rml.skolemization.skolemizationFactory;
import be.ugent.mmlab.rml.vocabulary.R2RMLVocabulary;
import be.ugent.mmlab.rml.vocabulary.RMLVocabulary;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
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
        public Repository replaceShortcuts(Repository mapDocRepo) {
        try {
            RepositoryConnection mapDocRepoCon = mapDocRepo.getConnection();

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

            for (URI uri : shortcutPredicates.keySet()) {
                RepositoryResult<Statement> shortcuts =
                        mapDocRepoCon.getStatements(null, uri, null, true);

                while (shortcuts.hasNext()) {
                    Statement st = shortcuts.next();
                    mapDocRepoCon.remove(st);
                    BNode blankMap = vf.createBNode();

                    URI pMap = vf.createURI(shortcutPredicates.get(uri).toString());
                    URI pConstant = vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                            + R2RMLVocabulary.R2RMLTerm.CONSTANT);
                    mapDocRepoCon.add(st.getSubject(),pMap, blankMap);
                    mapDocRepoCon.add(blankMap, pConstant,st.getObject());
                }      
                mapDocRepoCon.commit();
                mapDocRepoCon.close();
            }
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return mapDocRepo;
    }
    
    public Repository skolemizeStatements(Repository mapDocRepo) {
        try {
            RepositoryConnection mapDocRepoCon = mapDocRepo.getConnection();
            
            // Create new ArrayList.
            ArrayList<URI> elements = new ArrayList<>();
            elements.add(vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.SUBJECT_MAP));
            elements.add(vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.PREDICATE_MAP));
            elements.add(vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.OBJECT_MAP));
            elements.add(vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.PARENT_TRIPLES_MAP));
            elements.add(vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.JOIN_CONDITION));
            elements.add(vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.GRAPH_MAP));
            elements.add(vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.CLASS));
            elements.add(vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.CONSTANT));
            elements.add(vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.TEMPLATE));
            elements.add(vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.TERM_TYPE));
            elements.add(vf.createURI(RMLVocabulary.RML_NAMESPACE
                    + RMLVocabulary.RMLTerm.LOGICAL_SOURCE));
            elements.add(vf.createURI(RMLVocabulary.RML_NAMESPACE
                    + RMLVocabulary.RMLTerm.SOURCE));
            elements.add(vf.createURI(RMLVocabulary.RML_NAMESPACE
                    + RMLVocabulary.RMLTerm.REFERENCE_FORMULATION));
            elements.add(vf.createURI(RMLVocabulary.RML_NAMESPACE
                    + RMLVocabulary.RMLTerm.REFERENCE));
            elements.add(vf.createURI(RMLVocabulary.RML_NAMESPACE
                    + RMLVocabulary.RMLTerm.ITERATOR));
            
            for (URI uri : elements) {
                RepositoryResult<Statement> statements =
                        mapDocRepoCon.getStatements(null, uri, null, true);

                while (statements.hasNext()) {
                    Statement st = statements.next();
                    
                    Resource blankSubjectMap = st.getSubject();
                    Resource skolemizedSubjectMap = skolemizationFactory.skolemizeBlankNode(blankSubjectMap);
                    if (st.getSubject().toString().startsWith("_:")) {
                        skolemizationFactory.skolemSubstitution(
                                st.getSubject(), skolemizedSubjectMap, mapDocRepo);
                    }
                    
                    Value blankObjectMap = st.getObject();
                    Resource skolemizedObjectMap = skolemizationFactory.skolemizeBlankNode(blankObjectMap);
                    if (st.getObject().toString().startsWith("_:") && 
                            (  st.getObject().getClass() == Resource.class
                            || st.getObject().getClass() == Value.class
                            || st.getObject().getClass() == org.openrdf.sail.memory.model.MemBNode.class) ) {
                        skolemizationFactory.skolemSubstitution(
                                st.getObject(), skolemizedObjectMap, mapDocRepo);
                    }
                }
                mapDocRepoCon.commit();
                mapDocRepoCon.close();
            }
            
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return mapDocRepo;
    }
    
    

}
