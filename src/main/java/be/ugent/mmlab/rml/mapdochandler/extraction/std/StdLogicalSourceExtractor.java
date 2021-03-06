package be.ugent.mmlab.rml.mapdochandler.extraction.std;

import be.ugent.mmlab.rml.model.Source;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.source.std.StdSource;
import be.ugent.mmlab.rml.model.std.StdTriplesMap;
import be.ugent.mmlab.rml.vocabularies.R2RMLVocabulary;
import be.ugent.mmlab.rml.vocabularies.RMLVocabulary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;

/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : StdLogicalSourceExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */
public class StdLogicalSourceExtractor {

    // Log
    private static final Logger log = 
            LoggerFactory.getLogger(
            StdLogicalSourceExtractor.class.getSimpleName());

//    /**
//     *
//     * @return
//     */
//    public Map<Resource, Source> extractInputResources(Repository repository) {
//        Map<Resource, Source> inputResources = new HashMap<Resource, Source>();
//
//        RepositoryResult<Statement> statements = getInputResources(repository);
//
//        inputResources = putInputResources(repository, statements, inputResources);
//
//        return inputResources;
//    }

//    /**
//     *
//     * @return
//     */
//    protected RepositoryResult<Statement> getInputResources(Repository repository) {
//        RepositoryResult<Statement> inputStatements = null;
//        try {
//
//            RepositoryConnection connection = repository.getConnection();
//            ValueFactory vf = connection.getValueFactory();
//
//            IRI p = vf.createIRI(RMLVocabulary.RML_NAMESPACE
//                    + RMLVocabulary.RMLTerm.SOURCE);
//            inputStatements = connection.getStatements(null, p, null, true);
//
//            connection.close();
//
//        } catch (RepositoryException ex) {
//            log.error("RepositoryException " + ex);
//        }
//        return inputStatements;
//    }

    /**
     *
     * @param statements
     * @param inputResources
     * @return
     */
//    protected Map<Resource, Source> putInputResources(Repository repository,
//            RepositoryResult<Statement> statements, Map<Resource, Source> inputResources) {
//        try {
//            RepositoryConnection connection = repository.getConnection();
//            ValueFactory vf = connection.getValueFactory();
//
//            IRI p = vf.createIRI(RMLVocabulary.RML_NAMESPACE
//                    + RMLVocabulary.RMLTerm.LOGICAL_SOURCE);
//
//            while (statements.hasNext()) {
//                RepositoryResult<Statement> triplesMapsStatements =
//                        connection.getStatements(null, p, statements.next().getSubject(), true);
//
//                while (triplesMapsStatements.hasNext()) {
//                    try {
//                        inputResources.put(
//                                //triplesMap resource
//                                triplesMapsStatements.next().getSubject(),
//                                //input source
//                                new StdSource(triplesMapsStatements.next().getObject().stringValue()));
//                    } catch (Exception ex) {
//                        log.error(StdLogicalSourceExtractor.class.getName() + ex);
//                    }
//                }
//            }
//        } catch (RepositoryException ex) {
//            log.error("RepositoryException " + ex);
//        }
//        return inputResources;
//    }

    /**
     *
     * @param inputResource
     * @param inputResources
     */
//    public void extractInputSource(
//            Repository repository, Resource inputResource,
//            Map<Resource, Source> inputResources) {
//        log.debug(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
//                + "Extract Input Resource : "
//                + inputResource.stringValue());
//
//        Source result = inputResources.get(inputResource);
//
//        // Extract TriplesMap properties
//        Set<TriplesMap> triplesMaps =
//                extractTriplesMaps(repository, inputResource);
//
//        // Add triples maps
//        for (TriplesMap triplesMap : triplesMaps) {
//            //result.setTriplesMap(triplesMap);
//        }
//
//        log.info(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
//                + "Extract of Input source : "
//                + inputResource.stringValue() + " done.");
//    }

    /**
     *
     * @param inputResource
     * @return
     */
//    protected Set<TriplesMap> extractTriplesMaps(
//            Repository repository, Resource inputResource) {
//        Set<TriplesMap> triplesMaps = new HashSet<TriplesMap>();
//        try {
//            TriplesMap triplesMap;
//
//            RepositoryConnection connection = repository.getConnection();
//
//            IRI p = getTermURI(repository, RMLVocabulary.RMLTerm.LOGICAL_SOURCE);
//            RepositoryResult<Statement> triplesMapStatements =
//                    connection.getStatements(null, p, inputResource, true);
//
//            while (triplesMapStatements.hasNext()) {
//                triplesMap = new StdTriplesMap(
//                        null, null, null, triplesMapStatements.next().getSubject().stringValue());
//                triplesMaps.add(triplesMap);
//            }
//            connection.close();
//
//        } catch (RepositoryException ex) {
//            log.error("RepositoryException " + ex);
//        }
//        return triplesMaps;
//    }

    /**
     *
     * @param term
     * @param resource
     * @param triplesMap
     * @return
     */
//    protected RepositoryResult<Statement> getStatements(
//            Repository repository, Enum term, Resource resource, TriplesMap triplesMap) {
//        RepositoryResult<Statement> statements = null;
//        try {
//            IRI p = getTermURI(repository, term);
//            RepositoryConnection connection = repository.getConnection();
//            statements = connection.getStatements(resource, p, null, true);
//
//            connection.close();
//        } catch (RepositoryException ex) {
//            log.error("RepositoryException " + ex);
//        }
//        return statements;
//    }

    /**
     *
     * @param term
     * @return
     */
//    protected static IRI getTermURI(Repository repository, Enum term) {
//        String namespace = R2RMLVocabulary.R2RML_NAMESPACE;
//
//        if (term instanceof RMLVocabulary.RMLTerm) {
//            namespace = RMLVocabulary.RML_NAMESPACE;
//        } else if ((term instanceof R2RMLVocabulary.R2RMLTerm)) {
//            namespace = R2RMLVocabulary.R2RML_NAMESPACE;
//        } else {
//            log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
//                    + term + " is not valid.");
//        }
//
//        RepositoryConnection connection;
//        IRI uri = null;
//        try {
//            connection = repository.getConnection();
//            ValueFactory vf = connection.getValueFactory();
//            uri = vf.createIRI(namespace + term);
//            connection.close();
//        } catch (RepositoryException ex) {
//            log.error("RepositoryException " + ex);
//        }
//        return uri;
//    }
}
