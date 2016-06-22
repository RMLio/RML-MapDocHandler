package be.ugent.mmlab.rml.mapdochandler.extraction.concrete;

import be.ugent.mmlab.rml.mapdochandler.extraction.ConcreteSourceFactory;
import be.ugent.mmlab.rml.mapdochandler.extraction.condition.ConditionPredicateObjectMapExtractor;
import be.ugent.mmlab.rml.mapdochandler.extraction.source.concrete.LocalFileExtractor;
import be.ugent.mmlab.rml.model.RDFTerm.GraphMap;
import be.ugent.mmlab.rml.model.LogicalSource;
import be.ugent.mmlab.rml.model.PredicateObjectMap;
import be.ugent.mmlab.rml.model.RDFTerm.SubjectMap;
import be.ugent.mmlab.rml.model.ReferenceFormulation;
import be.ugent.mmlab.rml.model.Source;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdLogicalSource;
import be.ugent.mmlab.rml.vocabularies.CRMLVocabulary;
import be.ugent.mmlab.rml.vocabularies.QLVocabulary.QLTerm;
import be.ugent.mmlab.rml.vocabularies.R2RMLVocabulary;
import be.ugent.mmlab.rml.vocabularies.RMLVocabulary;
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
    static final Logger log = 
            LoggerFactory.getLogger(
            TriplesMapExtractor.class.getSimpleName());
       
    public void extractTriplesMap(
            Repository repository, Resource triplesMapSubject,
            Map<Resource, TriplesMap> triplesMapResources) {
        log.debug("Extract TriplesMap subject : "
                + triplesMapSubject.stringValue());
        TriplesMap result = triplesMapResources.get(triplesMapSubject);

        // Extract TriplesMap properties
        //Extracts at least one LogicalSource
        //TODO:check if it gets more than one Logical Sources
        LogicalSource logicalSource =
                extractLogicalSources(repository, triplesMapSubject, result);
        
        result.setLogicalSource(logicalSource);
        // Create a graph maps storage to save all met graph uri during parsing.
        GraphMap graphMap = null;

        // Extract exactly one SubjectMap
        //SubjectMap subjectMap = extractSubjectMap(
        //rmlMappingGraph, triplesMapSubject, graphMaps, result);
        SubjectMapExtractor sbjMapExtractor = new SubjectMapExtractor();
        SubjectMap subjectMap =
                sbjMapExtractor.extractSubjectMap(
                repository, triplesMapSubject, graphMap, result);
        
        try {
            result.setSubjectMap(subjectMap);
            } catch (Exception ex) {
                log.error("Exception: " + ex);
        }

        // Extract PredicateObjectMaps
        Set<PredicateObjectMap> predicateObjectMaps = extractPredicateObjectMaps(
                repository, triplesMapSubject, graphMap, result,
                triplesMapResources);

        // Extract zero or more PredicateObjectMaps
        for (PredicateObjectMap predicateObjectMap : predicateObjectMaps) {
            result.setPredicateObjectMap(predicateObjectMap);
        }

        log.debug("Extract of TriplesMap : "
                + triplesMapSubject.stringValue() + " done.");
    }
    
    protected LogicalSource extractLogicalSources(
            Repository repository, Resource triplesMapSubject, TriplesMap triplesMap) {
        RepositoryResult<Statement> sourceStatements;
        LogicalSource logicalSource = null;
        SourceExtractor sourceExtractor = null;
        ReferenceFormulation dialect = null;
        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();

            LogicalSourceExtractor logicalSourceExtractor = 
                    new LogicalSourceExtractor();
            Resource blankLogicalSource =
                    logicalSourceExtractor.
                    extractLogicalSource(
                    repository, triplesMapSubject, triplesMap);

            QLTerm referenceFormulation =
                    logicalSourceExtractor.getReferenceFormulation(
                    repository, blankLogicalSource, triplesMap);
            log.debug("Reference Formulation " + referenceFormulation);

            String iterator =
                    logicalSourceExtractor.getIterator(
                    repository, blankLogicalSource, triplesMap);
            log.debug("Iterator " + iterator);
            
            String table =
                    logicalSourceExtractor.getTableName(
                    repository, blankLogicalSource, triplesMap);
            log.debug("Table " + table);
            
            URI p = vf.createURI(
                    RMLVocabulary.RML_NAMESPACE 
                    + RMLVocabulary.RMLTerm.SOURCE);
            sourceStatements = 
                    connection.getStatements(
                    blankLogicalSource, p, null, true);

            while (sourceStatements.hasNext()) {
                Set<Source> inputSources;
                Statement sourceStatement = sourceStatements.next();

                //TODO:Align the following with ConcreteInputFactory
                if (sourceStatement.getObject().getClass().getSimpleName().equals("MemLiteral")) {
                    log.info("Literal-valued Input Source");
                    LocalFileExtractor input = new LocalFileExtractor();
                    inputSources = 
                            input.extractSources(
                            repository, sourceStatement.getObject());
                    
                } //object input
                else {
                    log.info("Resource-valued Input Source");
                    ConcreteSourceFactory inputFactory = new ConcreteSourceFactory();
                    sourceExtractor = inputFactory.createSourceExtractor(
                            repository, (Resource) sourceStatement.getObject());
                    inputSources = sourceExtractor.
                            extractSources(repository, sourceStatement.getObject());
                    log.debug("Source extracted : " + inputSources);
                }
                
                if(sourceExtractor != null && 
                        sourceExtractor.getClass().getSimpleName().equals("CsvwExtractor")){
                    
                    dialect = sourceExtractor.extractCustomReferenceFormulation(
                            repository,sourceStatement.getObject());
                }
                
                String query =
                    logicalSourceExtractor.getQuery(
                    repository, blankLogicalSource, triplesMap);
                log.debug("Query " + query);

                for (Source inputSource : inputSources) {
                    logicalSource = new StdLogicalSource(
                            iterator, inputSource, query, table,
                            referenceFormulation, dialect);
                }
                log.debug("Triples Map extracted");
            }

            log.debug("Logical source extracted : " + logicalSource);
            connection.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return logicalSource;
    }
    

    public Set<PredicateObjectMap> extractPredicateObjectMaps(
            Repository repository, Resource triplesMapSubject,
            GraphMap graphMap, TriplesMap result,
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
                    PredicateObjectMapExtractor preObjMapExtractor ;
                    Statement statement = statements.next();

                    if (connection.hasStatement(
                            (Resource) statement.getObject(),
                            vf.createURI(CRMLVocabulary.CRML_NAMESPACE
                            + CRMLVocabulary.cRMLTerm.BOOLEAN_CONDITION), null, true)) {
                        log.debug("Condition Predicate Object Map Extractor");
                        preObjMapExtractor = new ConditionPredicateObjectMapExtractor();
                    }
                    else {
                        if (connection.hasStatement(
                                (Resource) statement.getObject(),
                                vf.createURI(CRMLVocabulary.CRML_NAMESPACE
                                + CRMLVocabulary.cRMLTerm.FALLBACK),
                                null, true)) {
                            log.debug("Predicate Object Map with fallback POM");
                            preObjMapExtractor = new ConditionPredicateObjectMapExtractor();
                        } else {
                            log.debug("Simple Predicate Object Map Extractor");
                            preObjMapExtractor = new PredicateObjectMapExtractor();
                        }
                    }
                    PredicateObjectMap predicateObjectMap =
                            preObjMapExtractor.extractPredicateObjectMap(
                            repository, triplesMapSubject,
                            (Resource) statement.getObject(),
                            graphMap, triplesMapResources, result);
                    if (predicateObjectMap != null) {
                        predicateObjectMap.setOwnTriplesMap(result);
                        predicateObjectMaps.add(predicateObjectMap);
                    }
                }
            } catch (ClassCastException e) {
                log.error("ClassCastException " + e 
                        + " A resource was expected in object of predicateObjectMap of "
                        + triplesMapSubject.stringValue());
            }
            log.debug("Number of extracted predicate-object maps : "
                    + predicateObjectMaps.size());
            connection.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return predicateObjectMaps;
    }

}
