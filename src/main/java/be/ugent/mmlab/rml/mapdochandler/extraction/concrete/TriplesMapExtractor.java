package be.ugent.mmlab.rml.mapdochandler.extraction.concrete;

import be.ugent.mmlab.rml.input.extractor.concrete.ConcreteInputFactory;
import be.ugent.mmlab.rml.input.extractor.concrete.LocalFileExtractor;
import be.ugent.mmlab.rml.model.InputSource;
import be.ugent.mmlab.rml.model.RDFTerm.GraphMap;
import be.ugent.mmlab.rml.model.LogicalSource;
import be.ugent.mmlab.rml.model.PredicateObjectMap;
import be.ugent.mmlab.rml.model.RDFTerm.SubjectMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdLogicalSource;
import be.ugent.mmlab.rml.vocabulary.QLVocabulary;
import be.ugent.mmlab.rml.vocabulary.R2RMLVocabulary;
import be.ugent.mmlab.rml.vocabulary.RMLVocabulary;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : TriplesMapExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */
public class TriplesMapExtractor {
    
    //Log
    static final Logger log = LoggerFactory.getLogger(TriplesMapExtractor.class);
       
    public void extractTriplesMap(
            Repository repository, Resource triplesMapSubject,
            Map<Resource, TriplesMap> triplesMapResources) {
        log.debug(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                + "Extract TriplesMap subject : "
                + triplesMapSubject.stringValue());
        TriplesMap result = triplesMapResources.get(triplesMapSubject);

        // Extract TriplesMap properties
        //Extracts at least one LogicalSource
        //TODO:check if it get more than one Logical Sources
        LogicalSource logicalSource =
                extractLogicalSources(repository, triplesMapSubject, result);
        
        //String input = extractInput(rmlMappingGraph,triplesMapSubject);
        
        result.setLogicalSource(logicalSource);
        // Create a graph maps storage to save all met graph uri during parsing.
        Set<GraphMap> graphMaps = new HashSet<GraphMap>();

        // Extract exactly one SubjectMap
        //SubjectMap subjectMap = extractSubjectMap(rmlMappingGraph, triplesMapSubject, graphMaps, result);
        SubjectMapExtractor sbjMapExtractor = new SubjectMapExtractor();
        SubjectMap subjectMap =
                sbjMapExtractor.extractSubjectMap(repository, triplesMapSubject, graphMaps, result);
        
        try {
            result.setSubjectMap(subjectMap);
            } catch (Exception ex) {
                log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + ex);
        }

        // Extract PredicateObjectMaps
        Set<PredicateObjectMap> predicateObjectMaps = extractPredicateObjectMaps(
                repository, triplesMapSubject, graphMaps, result,
                triplesMapResources);

        // Extract zero or more PredicateObjectMaps
        for (PredicateObjectMap predicateObjectMap : predicateObjectMaps) {
            result.setPredicateObjectMap(predicateObjectMap);
        }

        log.debug(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                + "Extract of TriplesMap subject : "
                + triplesMapSubject.stringValue() + " done.");
    }
    
    protected LogicalSource extractLogicalSources(
            Repository repository, Resource triplesMapSubject, TriplesMap triplesMap) {
        RepositoryResult<Statement> sourceStatements;
        LogicalSource logicalSource = null;
        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();

            LogicalSourceExtractor logicalSourceExtractor = new LogicalSourceExtractor();
            Resource blankLogicalSource =
                    logicalSourceExtractor.extractLogicalSource(repository, triplesMapSubject, triplesMap);

            QLVocabulary.QLTerm referenceFormulation =
                    logicalSourceExtractor.getReferenceFormulation(
                    repository, triplesMapSubject, blankLogicalSource, triplesMap);

            String iterator =
                    logicalSourceExtractor.getIterator(
                    repository, triplesMapSubject, blankLogicalSource, triplesMap);

            URI p = vf.createURI(RMLVocabulary.RML_NAMESPACE + RMLVocabulary.RMLTerm.SOURCE);

            connection.getStatements(blankLogicalSource, p, null, true);

            p = vf.createURI(RMLVocabulary.RML_NAMESPACE + RMLVocabulary.RMLTerm.SOURCE);
            sourceStatements = connection.getStatements(blankLogicalSource, p, null, true);

            

            while (sourceStatements.hasNext()) {
                //Extract the file identifier
                String source;
                Set<InputSource> inputSources;
                Statement sourceStatement = sourceStatements.next();

                //TODO:Align the following with ConcreteInputFactory
                if (sourceStatement.getObject().getClass().getSimpleName().equals("MemLiteral")) {
                    log.info("Literal-valued Input Source");
                    source = sourceStatement.getObject().stringValue();
                    LocalFileExtractor input = new LocalFileExtractor();
                    inputSources = input.extractInput(repository, source);
                } //object input
                else {
                    log.info("Resource-valued Input Source");
                    ConcreteInputFactory inputFactory = new ConcreteInputFactory();
                    inputSources = inputFactory.chooseInput(
                            repository, (Resource) sourceStatement.getObject());
                }

                for (InputSource inputSource : inputSources) {
                    logicalSource = new StdLogicalSource(iterator, inputSource, referenceFormulation);
                }
            }

            log.debug(
                    Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "Logical source extracted : "
                    + logicalSource);
            connection.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return logicalSource;
    }

    
    public Set<PredicateObjectMap> extractPredicateObjectMaps(
            Repository repository, Resource triplesMapSubject,
            Set<GraphMap> graphMaps, TriplesMap result,
            Map<Resource, TriplesMap> triplesMapResources) {
        Set<PredicateObjectMap> predicateObjectMaps = null;
        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();
            // Extract predicate-object maps
            URI p = vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.PREDICATE_OBJECT_MAP);
            RepositoryResult<Statement> statements =
                    connection.getStatements(triplesMapSubject, p, null, true);

            predicateObjectMaps = new HashSet<PredicateObjectMap>();
            try {
                while (statements.hasNext()) {
                    PredicateObjectMapExtractor preObjMapExtractor = new PredicateObjectMapExtractor();
                    PredicateObjectMap predicateObjectMap = preObjMapExtractor.extractPredicateObjectMap(
                            repository, triplesMapSubject,
                            (Resource) statements.next().getObject(),
                            graphMaps, triplesMapResources, result);
                    if (predicateObjectMap != null) {
                        predicateObjectMap.setOwnTriplesMap(result);
                        predicateObjectMaps.add(predicateObjectMap);
                    }
                }
            } catch (ClassCastException e) {
                log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                        + "A resource was expected in object of predicateObjectMap of "
                        + triplesMapSubject.stringValue());
            }
            log.debug(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "extractPredicateObjectMaps] Number of extracted predicate-object maps : "
                    + predicateObjectMaps.size());
            connection.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return predicateObjectMaps;
    }

}
