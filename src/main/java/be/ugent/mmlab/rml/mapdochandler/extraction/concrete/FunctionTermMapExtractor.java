package be.ugent.mmlab.rml.mapdochandler.extraction.concrete;

import be.ugent.mmlab.rml.model.PredicateObjectMap;
import be.ugent.mmlab.rml.model.RDFTerm.*;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdFunctionTermMap;
import be.ugent.mmlab.rml.model.termMap.ReferenceMap;
import be.ugent.mmlab.rml.vocabularies.FnVocabulary;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static be.ugent.mmlab.rml.extraction.TermExtractor.extractValueFromTermMap;


/**
 * Created by andimou on 7/8/16.
 */
public class FunctionTermMapExtractor {

    // Log
    static final Logger log =
            LoggerFactory.getLogger(
                    FunctionTermMapExtractor.class.getSimpleName());

    public Set<FunctionTermMap> processFunctionTermMap(
            Repository repository, Resource object, Map<Resource,
            TriplesMap> triplesMapResources, TriplesMap triplesMap,
            PredicateObjectMap predicateObjectMap, GraphMap graphMap){
        Set<FunctionTermMap> results = new HashSet<>();
        FunctionTermMap result = null;
        Value value = null;
        log.debug("Extracting Function Term Map..");

        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();
            IRI pred = vf.createIRI(FnVocabulary.FnML_NAMESPACE + FnVocabulary.FnTerm.FUNCTION_VALUE);
            IRI function = null;
            Set<IRI> parameters = null;

            //Extract additional properties for Function Term Map
            IRI functionValue = (IRI) extractValueFromTermMap(repository, object, pred, triplesMap);

            if(functionValue != null) {
                TriplesMapExtractor triplesMapExtractor = new TriplesMapExtractor();
                TriplesMap functionTriplesMap =
                        triplesMapExtractor.extractAndReturnTriplesMap(repository, functionValue, triplesMapResources);

                function = getFunction(functionTriplesMap);
                parameters = getParameters(functionTriplesMap);

                Map<String,String> parametersRefs = getFunRefPairs(functionTriplesMap);

                Value constantValue = null;
                IRI dataType = null;
                String languageTag = null;
                String stringTemplate = null;
                IRI termType = null;
                //URI termType = new URIImpl(R2RMLVocabulary.R2RMLTerm.LITERAL.toString());
                String inverseExpression = null;
                ReferenceMap referenceValue = null;
                log.debug("Function Object Map extracted.");
                result = new StdFunctionTermMap(
                        constantValue, dataType, languageTag, stringTemplate, termType, inverseExpression, referenceValue,
                        predicateObjectMap, graphMap, functionTriplesMap, function, parameters, parametersRefs);
                results.add(result);
            }


        } catch (Exception ex) {
            log.error("Exception: " + ex);
        }
        return results;

    }

    private IRI getFunction(TriplesMap functionTriplesMap){
        Set<PredicateObjectMap> predObjMaps = functionTriplesMap.getPredicateObjectMaps();
        IRI funPredicateURI = null;

        for(PredicateObjectMap predicateObjectMap : predObjMaps){
            log.debug("Retrieving the function...");
            PredicateMap funPredicate = predicateObjectMap.getPredicateMaps().iterator().next();
            Object executes = FnVocabulary.FNO_NAMESPACE + FnVocabulary.FnTerm.EXECUTES;
            String funPredicateValue = funPredicate.getConstantValue().stringValue();

            if(funPredicateValue.equals(executes)){
                SimpleValueFactory vf = SimpleValueFactory.getInstance();
                funPredicateURI = vf.createIRI(funPredicateValue);
            }

        }
        return funPredicateURI;
    }

    private Map<String,String> getFunRefPairs(TriplesMap functionTriplesMap){
        Map<String,String> parameters = new HashMap<String,String>();

        Set<PredicateObjectMap> predObjMaps = functionTriplesMap.getPredicateObjectMaps();

        for(PredicateObjectMap predicateObjectMap : predObjMaps) {
            String parameterValue;
            ObjectMap parameter;
            PredicateMap funPredicate = predicateObjectMap.getPredicateMaps().iterator().next();
            String funPredicateValue = funPredicate.getConstantValue().stringValue();

            String executes = FnVocabulary.FNO_NAMESPACE + FnVocabulary.FnTerm.EXECUTES;

            if(!funPredicateValue.equals(executes)) {
                parameter = predicateObjectMap.getObjectMaps().iterator().next();
                if(parameter.getReferenceMap() != null)
                    parameterValue = parameter.getReferenceMap().getReference();
                else
                    parameterValue = parameter.getConstantValue().stringValue();
                if(funPredicateValue != null && parameterValue != null)
                    parameters.put(funPredicateValue, parameterValue);
            }
        }
        return parameters;
    }


    private Set<IRI> getParameters(TriplesMap functionTriplesMap){
        Set<PredicateObjectMap> predObjMaps = functionTriplesMap.getPredicateObjectMaps();
        Set<IRI> parameters = new HashSet<IRI>();
        IRI parameter = null;

        for(PredicateObjectMap predicateObjectMap : predObjMaps){
            log.debug("Retrieving the function...");
            PredicateMap funPredicate = predicateObjectMap.getPredicateMaps().iterator().next();

            Object executes = FnVocabulary.FNO_NAMESPACE + FnVocabulary.FnTerm.EXECUTES;
            String funPredicateValue = funPredicate.getConstantValue().stringValue();

            if(!funPredicateValue.equals(executes)){
                ValueFactory vf = SimpleValueFactory.getInstance();
                parameter = vf.createIRI(funPredicateValue);
                parameters.add(parameter);
            }

        }
        return parameters;
    }
}
