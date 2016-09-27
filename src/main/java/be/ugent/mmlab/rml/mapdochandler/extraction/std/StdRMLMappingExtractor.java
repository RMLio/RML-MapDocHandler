package be.ugent.mmlab.rml.mapdochandler.extraction.std;

import be.ugent.mmlab.rml.mapdochandler.extraction.RMLMappingExtractor;
import be.ugent.mmlab.rml.mapdochandler.skolemization.skolemizationFactory;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdTriplesMap;
import be.ugent.mmlab.rml.vocabularies.R2RMLVocabulary;
import be.ugent.mmlab.rml.vocabularies.RMLVocabulary;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
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
public class StdRMLMappingExtractor implements RMLMappingExtractor{
    
    // Log
    private static final Logger log = 
            LoggerFactory.getLogger(
            StdRMLMappingExtractor.class.getSimpleName());
    // Value factory
    private static ValueFactory vf = SimpleValueFactory.getInstance();
    private boolean skolemization = false;
    
    public StdRMLMappingExtractor(){

    }
    
    public StdRMLMappingExtractor(boolean skolemization){
        this.skolemization = skolemization;
    }
   
    /**
     * Construct TriplesMap objects rule. A triples map is represented by a
     * resource that references the following other resources : - It must have
     * exactly one subject map * using the rr:subjectMap property.
     *
     * @return
     */
    @Override
    public Map<Resource, TriplesMap> extractTriplesMapResources(Repository repo) {
        Map<Resource, TriplesMap> triplesMapResources = new HashMap<Resource, TriplesMap>();

        RepositoryResult<Statement> statements = getTriplesMapResources(repo);

        triplesMapResources = putTriplesMapResources(statements, triplesMapResources);

        return triplesMapResources;
    }
    
    /**
     *
     * @return
     */
    protected RepositoryResult<Statement> getTriplesMapResources(Repository repo) {
        RepositoryResult<Statement> statements = null;

        try {
            RepositoryConnection connection = repo.getConnection();

            IRI o = vf.createIRI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.TRIPLES_MAP_CLASS);
            IRI p = vf.createIRI(RMLVocabulary.RML_NAMESPACE
                    + RMLVocabulary.RMLTerm.LOGICAL_SOURCE);

            statements = connection.getStatements(null, p, null, true);
            log.debug("Triples Map statements were retrieved: " + statements.hasNext());
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return statements;
    }

    /**
     *
     * @param statements
     * @param triplesMapResources
     * @return
     */
    protected Map<Resource, TriplesMap> putTriplesMapResources(
            RepositoryResult<Statement> statements, Map<Resource, TriplesMap> triplesMapResources) {
        try {
            while (statements.hasNext()) {
                Statement statement = statements.next();
                triplesMapResources.put(statement.getSubject(),
                        new StdTriplesMap(null, null, null, statement.getSubject().stringValue()));
            }

        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return triplesMapResources;
    }
    
    /**
     * Constant-valued term maps can be expressed more concisely using the
     * constant shortcut properties rr:subject, rr:predicate, rr:object and
     * rr:graph. Occurrences of these properties must be treated exactly as if
     * the following triples were present in the mapping graph instead.
     *
     */
    @Override
        public Repository replaceShortcuts(Repository mapDocRepo) {
        Map<IRI, IRI> shortcutPredicates = new HashMap<IRI, IRI>();

        try {
            RepositoryConnection mapDocRepoCon = mapDocRepo.getConnection();

            shortcutPredicates.put(
                    vf.createIRI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.SUBJECT),
                    vf.createIRI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.SUBJECT_MAP));
            shortcutPredicates.put(
                    vf.createIRI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.PREDICATE),
                    vf.createIRI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.PREDICATE_MAP));
            shortcutPredicates.put(vf.createIRI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.OBJECT),
                    vf.createIRI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.OBJECT_MAP));
            shortcutPredicates
                    .put(vf.createIRI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.GRAPH),
                    vf.createIRI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.GRAPH_MAP));

            for (IRI uri : shortcutPredicates.keySet()) {
                RepositoryResult<Statement> shortcuts =
                        mapDocRepoCon.getStatements(null, uri, null, true);

                while (shortcuts.hasNext()) {
                    Statement st = shortcuts.next();
                    mapDocRepoCon.remove(st);
                    BNode blankMap = vf.createBNode();

                    IRI pMap = vf.createIRI(shortcutPredicates.get(uri).toString());
                    IRI pConstant = vf.createIRI(R2RMLVocabulary.R2RML_NAMESPACE
                            + R2RMLVocabulary.R2RMLTerm.CONSTANT);
                    mapDocRepoCon.add(st.getSubject(), pMap, blankMap);
                    mapDocRepoCon.add(blankMap, pConstant, st.getObject());
                }
            }
            mapDocRepoCon.commit();
            mapDocRepoCon.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return mapDocRepo;
    }
    
    @Override
    public Repository skolemizeStatements(Repository mapDocRepo) {
        try {
            RepositoryConnection mapDocRepoCon = mapDocRepo.getConnection();
            
            // Create new ArrayList.
            ArrayList<IRI> elements = new ArrayList<>();
            elements.add(vf.createIRI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.SUBJECT_MAP));
            elements.add(vf.createIRI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.PREDICATE_MAP));
            elements.add(vf.createIRI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.OBJECT_MAP));
            elements.add(vf.createIRI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.PARENT_TRIPLES_MAP));
            elements.add(vf.createIRI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.JOIN_CONDITION));
            elements.add(vf.createIRI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.GRAPH_MAP));
            elements.add(vf.createIRI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.CLASS));
            elements.add(vf.createIRI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.CONSTANT));
            elements.add(vf.createIRI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.TEMPLATE));
            elements.add(vf.createIRI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.TERM_TYPE));
            elements.add(vf.createIRI(RMLVocabulary.RML_NAMESPACE
                    + RMLVocabulary.RMLTerm.LOGICAL_SOURCE));
            elements.add(vf.createIRI(RMLVocabulary.RML_NAMESPACE
                    + RMLVocabulary.RMLTerm.SOURCE));
            elements.add(vf.createIRI(RMLVocabulary.RML_NAMESPACE
                    + RMLVocabulary.RMLTerm.REFERENCE_FORMULATION));
            elements.add(vf.createIRI(RMLVocabulary.RML_NAMESPACE
                    + RMLVocabulary.RMLTerm.REFERENCE));
            elements.add(vf.createIRI(RMLVocabulary.RML_NAMESPACE
                    + RMLVocabulary.RMLTerm.ITERATOR));
            
            for (IRI uri : elements) {
                RepositoryResult<Statement> statements =
                        mapDocRepoCon.getStatements(null, uri, null, true);

                while (statements.hasNext()) {
                    Statement st = statements.next();
                    
                    //Skolemize subject
                    Resource blankSubjectMap = st.getSubject();
                    Resource skolemizedSubjectMap = 
                            skolemizationFactory.skolemizeBlankNode(blankSubjectMap);
                    
                    if (st.getSubject().toString().startsWith("_:")) {
                        skolemizationFactory.skolemSubstitution(
                                st.getSubject(), skolemizedSubjectMap, mapDocRepo);
                    }
                    
                    //skolemize object
                    Value blankObjectMap = st.getObject();
                    Resource skolemizedObjectMap = 
                            skolemizationFactory.skolemizeBlankNode(blankObjectMap);
                    
                    if (st.getObject().toString().startsWith("_:") && 
                            (  st.getObject().getClass() == Resource.class
                            || st.getObject().getClass() == Value.class
                            || st.getObject().getClass() == 
                            org.eclipse.rdf4j.sail.memory.model.MemBNode.class) ) {
                        skolemizationFactory.skolemSubstitution(
                                st.getObject(), skolemizedObjectMap, mapDocRepo);
                    }
                }
            }
            mapDocRepoCon.commit();
            mapDocRepoCon.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return mapDocRepo;
    }
    
    

}
