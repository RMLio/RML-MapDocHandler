package be.ugent.mmlab.rml.mapdochandler.extraction.concrete;

import be.ugent.mmlab.rml.condition.extractor.FallbackMapExtractor;
import be.ugent.mmlab.rml.condition.model.Condition;
import be.ugent.mmlab.rml.mapdochandler.extraction.condition.ConditionPredicateObjectMapExtractor;
import be.ugent.mmlab.rml.model.RDFTerm.*;
import be.ugent.mmlab.rml.model.PredicateObjectMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdConditionPredicateObjectMap;
import be.ugent.mmlab.rml.model.std.StdPredicateObjectMap;
import be.ugent.mmlab.rml.vocabularies.CRMLVocabulary;
import be.ugent.mmlab.rml.vocabularies.R2RMLVocabulary;
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
 * RML - Mapping Document Handler : PredicateObjectMapExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */
public class PredicateObjectMapExtractor {

    public static int counter = 0;
    
    // Log
    static final Logger log =
            LoggerFactory.getLogger(
            PredicateObjectMapExtractor.class.getSimpleName());
    
    public PredicateObjectMap extractPredicateObjectMap(
            Repository repository,
            Resource triplesMapSubject,
            Resource predicateObject,
            GraphMap savedGraphMap,
            Map<Resource, TriplesMap> triplesMapResources,
            TriplesMap triplesMap) {
        RepositoryResult<Statement> predicate_statements;
        RepositoryResult<Statement> object_statements;
        PredicateObjectMap predicateObjectMap = null;
        Set<PredicateMap> predicateMaps = new HashSet<PredicateMap>();
        Set<ObjectMap> objectMaps = new HashSet<ObjectMap>();
        Set<ReferencingObjectMap> refObjectMaps = new HashSet<ReferencingObjectMap>();
        Set<FunctionTermMap> funObjectMaps = new HashSet<FunctionTermMap>();

        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();
            
            IRI p = vf.createIRI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.PREDICATE_MAP);
            predicate_statements = connection.getStatements(predicateObject, p, null, true);
            log.debug("Extracting predicate maps: " + predicateObject.stringValue() + " " + p);
            log.debug("More Predicate Map statements: " 
                    + predicate_statements.hasNext());
            while (predicate_statements.hasNext()) {
                Statement predicate_statement = predicate_statements.next();
                PredicateMapExtractor predMapExtractor = new PredicateMapExtractor();
                PredicateMap predicateMap = predMapExtractor.extractPredicateMap(
                        repository, predicate_statement,
                        savedGraphMap, triplesMap);
                predicateMaps.add(predicateMap);

                IRI o = vf.createIRI(R2RMLVocabulary.R2RML_NAMESPACE
                        + R2RMLVocabulary.R2RMLTerm.OBJECT_MAP);
                // Extract object maps
                object_statements = connection.getStatements(predicateObject, o, null, true);
                log.debug("Extracting Object Statements: " + predicateObject.toString() + ", " + o.stringValue());

                int counter = 0;
                while(object_statements.hasNext()) {
                    log.debug(object_statements.next().getSubject().stringValue());
                    counter += 1;
                }


                object_statements = connection.getStatements(predicateObject, o, null, true);

                if(object_statements.hasNext())
                while (object_statements.hasNext()) {

                    Statement object_statement = object_statements.next();
                    log.debug("Object_statement= " + object_statement.getSubject() + " " + object_statement.getPredicate() + " " + object_statement.getObject());
                    //Extract Referencing Object Maps
                    log.debug("Extracting Referencing Object Maps..");
                    ReferencingObjectMapExtractor refObjMapExtractor = 
                            new ReferencingObjectMapExtractor();
                    refObjectMaps = refObjMapExtractor.processReferencingObjectMap(
                            repository, object_statement, savedGraphMap,
                            triplesMapResources, triplesMap, triplesMapSubject, predicateObject);
                    log.debug("Referencing Object Map statements found: " + refObjectMaps);

                    //Extracting Function Term Map
                    if (refObjectMaps.isEmpty()) {
                        log.debug("Extracting Function Object Maps..");
                        FunctionTermMapExtractor funObjMapExtractor =
                                new FunctionTermMapExtractor();
                        funObjectMaps = funObjMapExtractor.processFunctionTermMap(
                                repository, (Resource) object_statement.getObject(),
                                triplesMapResources, triplesMap, predicateObjectMap, savedGraphMap);
                        log.debug("Function Object Map statements found: " + funObjectMaps);
                    }

                    //Extracting Simple Object Map
                    if (refObjectMaps.isEmpty() && funObjectMaps.isEmpty()) {
                        ObjectMapExtractor objMapExtractor = new ObjectMapExtractor();

                        ObjectMap objectMap = objMapExtractor.extractObjectMap(repository,
                                (Resource) object_statement.getObject(), savedGraphMap, triplesMap, triplesMapResources);

                        //COMBUST specific annotation
                        CombustExtractor combustExtractor = new CombustExtractor();
                        boolean valueComplete = combustExtractor.exrtactComplete(
                                (Resource) object_statement.getObject(), repository);
                        if (valueComplete) {
                            objectMap.setCompletion();
                        }
                        log.debug("to be completed " + predicateMap.getCompletion());

                        //COMBUST specific annotation to validate an object value
                        combustExtractor = new CombustExtractor();
                        boolean valueValidate = combustExtractor.exrtactValidate(
                                (Resource) object_statement.getObject(), repository);
                        if (valueValidate) {
                            objectMap.setValidation();
                        }

                        try {
                            objectMap.setOwnTriplesMap(triplesMapResources.get(triplesMapSubject));
                        } catch (Exception ex) {
                            log.error("Exception " + ": " + ex);
                        }
                        objectMaps.add(objectMap);
                    }
                    predicateObjectMap = new StdPredicateObjectMap(
                            predicateMaps, objectMaps, refObjectMaps);



                    predicateObjectMap = createPredicateObjectMap(
                            repository, triplesMapSubject,
                            predicateObject, savedGraphMap, triplesMapResources,
                            triplesMap, objectMaps, refObjectMaps, predicateMaps,funObjectMaps);
                    
                    /*predicateObjectMap = new StdPredicateObjectMap(
                            predicateMaps, objectMaps, refObjectMaps);*/
                    if (predicateObjectMap != null) {
                        GraphMapExtractor graphMapExtractor = new GraphMapExtractor();
                        graphMapExtractor.processGraphMaps(
                                repository, predicateObject, triplesMap,
                                predicateObjectMap, savedGraphMap);

                        //Extract dcterms:type if exists
                        try {
                            RepositoryConnection dcTermsConnection = repository.getConnection();
                            RepositoryResult<Statement> dcTermsStatements = dcTermsConnection.getStatements(predicateObject, repository.getValueFactory().createIRI("http://purl.org/dc/terms/type"), null, false);
                            if( dcTermsStatements != null && dcTermsStatements.hasNext()) {
                                predicateObjectMap.setDCTermsType(dcTermsStatements.next().getObject().stringValue());
                            }
                            dcTermsConnection.close();
                        } catch (RepositoryException ex) {
                            log.error("Exception: " + ex);
                        }

                    }
                    log.debug("Extract predicate-object map done.");

                }
                else {
                    predicateObjectMap = null;
                    return predicateObjectMap;
                }
            }
            connection.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return predicateObjectMap;
    }
    
    public PredicateObjectMap createPredicateObjectMap(
            Repository repository,
            Resource triplesMapSubject,
            Resource predicateObject,
            GraphMap savedGraphMap,
            Map<Resource, TriplesMap> triplesMapResources,
            TriplesMap triplesMap,
            Set<ObjectMap> objectMaps,
            Set<ReferencingObjectMap> refObjectMaps,
            Set<PredicateMap> predicateMaps,
            Set<FunctionTermMap> functionTermMaps) {
        PredicateObjectMap predicateObjectMap = null;
        Set<PredicateObjectMap> fallbackPOMs = new HashSet();
        
        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();

            counter += 1;
            log.debug("Counter: " + counter);
            log.debug("Create POM statement: " + predicateObject.stringValue() + ", " + vf.createIRI(CRMLVocabulary.CRML_NAMESPACE
                    + CRMLVocabulary.cRMLTerm.BOOLEAN_CONDITION + ", " +  true));
            if (connection.hasStatement(
                    predicateObject, vf.createIRI(CRMLVocabulary.CRML_NAMESPACE
                    + CRMLVocabulary.cRMLTerm.BOOLEAN_CONDITION), null, true)) {
                ConditionPredicateObjectMapExtractor preObjMapExtractor =
                        new ConditionPredicateObjectMapExtractor();
                Set<Condition> conditions = preObjMapExtractor.extractConditions(
                        repository, predicateObject, triplesMapResources, triplesMap);

                //Extracting fallbacks
                FallbackMapExtractor fallbackExtractor =
                        new FallbackMapExtractor();
                RepositoryResult<Statement> fallbackStatements =
                        fallbackExtractor.extractFallbackObjectMap(
                        repository, predicateObject, null, triplesMapResources,
                        triplesMap, triplesMapSubject, predicateObject);
                log.debug("Processing fallbacks...");
                if(fallbackStatements != null)
                while (fallbackStatements.hasNext()) {
                    Statement fallbackStatement = fallbackStatements.next();

                    //Extract Referencing Object Maps
                    log.debug("Extracting fallback POMs..");
                    PredicateObjectMapExtractor fallbackPreObjMapExtractor =
                            new PredicateObjectMapExtractor();
                    PredicateObjectMap fallbackPredicateObjectMap =
                            fallbackPreObjMapExtractor.extractPredicateObjectMap(
                            repository, triplesMapSubject,
                            (Resource) fallbackStatement.getObject(),
                            savedGraphMap, triplesMapResources, triplesMap);
                    fallbackPOMs.add(fallbackPredicateObjectMap);
                    predicateObjectMap = new StdConditionPredicateObjectMap(predicateMaps, objectMaps,
                            refObjectMaps, conditions, fallbackPOMs);
                }
                else{
                    log.debug("Simple POM extracted .");
                    predicateObjectMap = new StdPredicateObjectMap(
                            predicateMaps, objectMaps, refObjectMaps, functionTermMaps);
                }
            } else {
                log.debug("Simple POM extracted.");
                predicateObjectMap = new StdPredicateObjectMap(
                            predicateMaps, objectMaps, refObjectMaps, functionTermMaps);
            }
            
        } catch (RepositoryException ex) {
            log.error("Repository Exception " + ex);
        } finally {
            counter--;
            return predicateObjectMap;
        }
    }
    
}
