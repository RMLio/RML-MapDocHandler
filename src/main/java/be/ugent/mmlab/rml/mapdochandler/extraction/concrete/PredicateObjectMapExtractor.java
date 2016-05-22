package be.ugent.mmlab.rml.mapdochandler.extraction.concrete;

import be.ugent.mmlab.rml.condition.model.Condition;
import be.ugent.mmlab.rml.mapdochandler.extraction.condition.ConditionPredicateObjectMapExtractor;
import be.ugent.mmlab.rml.model.RDFTerm.GraphMap;
import be.ugent.mmlab.rml.model.RDFTerm.ObjectMap;
import be.ugent.mmlab.rml.model.RDFTerm.PredicateMap;
import be.ugent.mmlab.rml.model.PredicateObjectMap;
import be.ugent.mmlab.rml.model.RDFTerm.ReferencingObjectMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdPredicateObjectMap;
import be.ugent.mmlab.rml.vocabularies.CRMLVocabulary;
import be.ugent.mmlab.rml.vocabularies.R2RMLVocabulary;
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
 * RML - Mapping Document Handler : PredicateObjectMapExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */
public class PredicateObjectMapExtractor {
    
    // Log
    static final Logger log = 
            LoggerFactory.getLogger(
            PredicateObjectMapExtractor.class.getSimpleName());
    
    public PredicateObjectMap extractPredicateObjectMap(
            Repository repository,
            Resource triplesMapSubject,
            Resource predicateObject,
            Set<GraphMap> savedGraphMaps,
            Map<Resource, TriplesMap> triplesMapResources,
            TriplesMap triplesMap) {
        RepositoryResult<Statement> predicate_statements;
        RepositoryResult<Statement> object_statements;
        PredicateObjectMap predicateObjectMap = null;
        Set<PredicateMap> predicateMaps = new HashSet<PredicateMap>();
        Set<ObjectMap> objectMaps = new HashSet<ObjectMap>();
        Set<ReferencingObjectMap> refObjectMaps = new HashSet<ReferencingObjectMap>();
        
        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();
            
            if (connection.hasStatement(
                    predicateObject, vf.createURI(CRMLVocabulary.CRML_NAMESPACE
                    + CRMLVocabulary.cRMLTerm.BOOLEAN_CONDITION), null, true)) {
                log.debug("Conditional POM extracted.");
                ConditionPredicateObjectMapExtractor preObjMapExtractor =
                        new ConditionPredicateObjectMapExtractor();
                Set<Condition> conditions = preObjMapExtractor.extractConditions(
                                       repository, predicateObject);
                log.debug(conditions.size() + " conditions were found");
            } else {
                log.debug("Simple POM extracted.");
            }

            URI p = vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.PREDICATE_MAP);
            predicate_statements = connection.getStatements(predicateObject, p, null, true);
            log.debug("More Predicate Map statements: " 
                    + predicate_statements.hasNext());

            while (predicate_statements.hasNext()) {
                PredicateMapExtractor predMapExtractor = new PredicateMapExtractor();
                PredicateMap predicateMap = predMapExtractor.extractPredicateMap(
                        repository, predicate_statements.next(),
                        savedGraphMaps, triplesMap);
                predicateMaps.add(predicateMap);


                URI o = vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                        + R2RMLVocabulary.R2RMLTerm.OBJECT_MAP);
                // Extract object maps
                object_statements = connection.getStatements(predicateObject, o, null, true);
                log.debug("Object Map statements found: " + object_statements);

                while (object_statements.hasNext()) {
                    Statement object_statement = object_statements.next();

                    //Extract Referencing Object Maps
                    log.debug("Extracting Referencing Object Maps..");
                    ReferencingObjectMapExtractor refObjMapExtractor = 
                            new ReferencingObjectMapExtractor();
                    refObjectMaps = refObjMapExtractor.processReferencingObjectMap(
                            repository, object_statement, savedGraphMaps,
                            triplesMapResources, triplesMap, triplesMapSubject, predicateObject);
                    log.debug("Referencing Object Map statements found: " + refObjectMaps);
                    
                    if (refObjectMaps.isEmpty()) {
                        ObjectMapExtractor objMapExtractor = new ObjectMapExtractor();

                        ObjectMap objectMap = objMapExtractor.extractObjectMap(repository,
                                (Resource) object_statement.getObject(), savedGraphMaps, triplesMap);
                        try {
                            objectMap.setOwnTriplesMap(triplesMapResources.get(triplesMapSubject));
                        } catch (Exception ex) {
                            log.error("Exception " + ": " + ex);
                        }
                        objectMaps.add(objectMap);
                    }
                    predicateObjectMap = new StdPredicateObjectMap(
                            predicateMaps, objectMaps, refObjectMaps);
                    GraphMapExtractor graphMapExtractor = new GraphMapExtractor();
                    graphMapExtractor.processGraphMaps(
                            repository, predicateObject, triplesMap, predicateObjectMap, savedGraphMaps);

                    log.debug("Extract predicate-object map done.");

                }
            }
            connection.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return predicateObjectMap;
    }
    
}
