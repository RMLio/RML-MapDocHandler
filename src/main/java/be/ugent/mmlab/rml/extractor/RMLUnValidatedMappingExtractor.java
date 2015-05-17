package be.ugent.mmlab.rml.extractor;

import be.ugent.mmlab.rml.condition.extractor.BindConditionExtractor;
import be.ugent.mmlab.rml.condition.extractor.EqualConditionExtractor;
import be.ugent.mmlab.rml.condition.extractor.ProcessConditionExtractor;
import be.ugent.mmlab.rml.condition.extractor.SplitConditionExtractor;
import be.ugent.mmlab.rml.input.extractor.concrete.ConcreteInputFactory;
import be.ugent.mmlab.rml.model.GraphMap;
import be.ugent.mmlab.rml.model.JoinCondition;
import be.ugent.mmlab.rml.model.LogicalSource;
import be.ugent.mmlab.rml.model.ObjectMap;
import be.ugent.mmlab.rml.model.PredicateMap;
import be.ugent.mmlab.rml.model.PredicateObjectMap;
import be.ugent.mmlab.rml.model.ReferencingObjectMap;
import be.ugent.mmlab.rml.model.std.StdGraphMap;
import be.ugent.mmlab.rml.model.std.StdJoinCondition;
import be.ugent.mmlab.rml.model.std.StdLogicalSource;
import be.ugent.mmlab.rml.model.std.StdObjectMap;
import be.ugent.mmlab.rml.model.std.StdPredicateMap;
import be.ugent.mmlab.rml.model.std.StdPredicateObjectMap;
import be.ugent.mmlab.rml.model.std.StdReferencingObjectMap;
import be.ugent.mmlab.rml.model.std.StdSubjectMap;
import be.ugent.mmlab.rml.model.std.StdTriplesMap;
import be.ugent.mmlab.rml.model.SubjectMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.condition.model.BindCondition;
import be.ugent.mmlab.rml.condition.model.EqualCondition;
import be.ugent.mmlab.rml.condition.model.ProcessCondition;
import be.ugent.mmlab.rml.condition.model.SplitCondition;
import be.ugent.mmlab.rml.model.reference.ReferenceIdentifier;
import be.ugent.mmlab.rml.model.reference.ReferenceIdentifierImpl;
import be.ugent.mmlab.rml.processor.RMLProcessorFactory;
import be.ugent.mmlab.rml.processor.concrete.ConcreteRMLProcessorFactory;
import be.ugent.mmlab.rml.sesame.RMLSesameDataSet;
import be.ugent.mmlab.rml.vocabulary.HydraVocabulary;
import be.ugent.mmlab.rml.vocabulary.QLVocabulary;
import be.ugent.mmlab.rml.vocabulary.R2RMLVocabulary;
import be.ugent.mmlab.rml.vocabulary.RMLVocabulary;
import be.ugent.mmlab.rml.vocabulary.Term;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLStructureException;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLSyntaxException;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.R2RMLDataError;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;

/**
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
    
    protected List<Statement> getTriplesMapResources(RMLSesameDataSet rmlMappingGraph){
        
        URI o = rmlMappingGraph.URIref(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.TRIPLES_MAP_CLASS);
        URI p = rmlMappingGraph.URIref("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        List<Statement> statements = rmlMappingGraph.tuplePattern(null, p,
                o);
        return statements;
    }
    
    protected Map<Resource, TriplesMap> putTriplesMapResources(
            List<Statement> statements, Map<Resource, TriplesMap> triplesMapResources) {
        for (Statement s : statements) {
            try {
                triplesMapResources.put(s.getSubject(),
                        new StdTriplesMap(null, null, null, s.getSubject().stringValue()));
            } catch (InvalidR2RMLStructureException ex) {
                java.util.logging.Logger.getLogger(RMLUnValidatedMappingExtractor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return triplesMapResources;
    }
    
    /**
     * Extracts a TriplesMap properties:
     * Logical Source, SubjectMap and PredicateObjectMaps (if any)
     *
     * @param rmlMappingGraph
     * @param triplesMapSubject
     * @param triplesMapResources
     */
    @Override
    public void extractTriplesMap(
            RMLSesameDataSet rmlMappingGraph, Resource triplesMapSubject,
            Map<Resource, TriplesMap> triplesMapResources) {
        log.debug(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                + "Extract TriplesMap subject : "
                + triplesMapSubject.stringValue());
        TriplesMap result = triplesMapResources.get(triplesMapSubject);

        // Extract TriplesMap properties
        //Extracts at least one LogicalSource
        LogicalSource logicalSource =
                extractLogicalSources(rmlMappingGraph, triplesMapSubject, result);
        //String input = extractInput(rmlMappingGraph,triplesMapSubject);
        
        result.setLogicalSource(logicalSource);
        // Create a graph maps storage to save all met graph uri during parsing.
        Set<GraphMap> graphMaps = new HashSet<GraphMap>();

        // Extract exactly one SubjectMap
        SubjectMap subjectMap =
                extractSubjectMap(rmlMappingGraph, triplesMapSubject, graphMaps, result);
        try {
            result.setSubjectMap(subjectMap);
        } catch (InvalidR2RMLStructureException ex) {
            java.util.logging.Logger.getLogger(
                    RMLUnValidatedMappingExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Extract PredicateObjectMaps
        Set<PredicateObjectMap> predicateObjectMaps = extractPredicateObjectMaps(
                rmlMappingGraph, triplesMapSubject, graphMaps, result,
                triplesMapResources);

        // Extract zero or more PredicateObjectMaps
        for (PredicateObjectMap predicateObjectMap : predicateObjectMaps) {
            result.addPredicateObjectMap(predicateObjectMap);
        }

        log.info(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                + "Extract of TriplesMap subject : "
                + triplesMapSubject.stringValue() + " done.");
    }
    
    public String extractInput(RMLSesameDataSet rmlMappingGraph, Resource resource) {
        
        //URI predicate = rmlMappingGraph.URIref("http://www.w3.org/ns/hydra/core#template");
        URI predicate = RDF.TYPE;
         List<Statement> statements = rmlMappingGraph.tuplePattern(
                        (Resource) resource, predicate, null);
         
         return statements.get(0).getObject().stringValue();
         
    }
    
    protected LogicalSource extractLogicalSources(
            RMLSesameDataSet rmlMappingGraph, Resource triplesMapSubject, TriplesMap triplesMap) {

        Resource blankLogicalSource = 
                extractLogicalSource(rmlMappingGraph, triplesMapSubject, triplesMap);
        
        QLVocabulary.QLTerm referenceFormulation =
                getReferenceFormulation(rmlMappingGraph, triplesMapSubject, blankLogicalSource, triplesMap);

        //Extract the iterator to create the iterator. Some formats have null, like CSV or SQL
        List<Statement> iterators = getStatements(
                rmlMappingGraph, blankLogicalSource,
                RMLVocabulary.RML_NAMESPACE, RMLVocabulary.RMLTerm.ITERATOR, triplesMap);
            
        List<Statement> sourceStatements = getStatements(
                rmlMappingGraph,blankLogicalSource,
                RMLVocabulary.RML_NAMESPACE, RMLVocabulary.RMLTerm.SOURCE, triplesMap);
        
        List<Statement> splitStatements = getStatements(
                rmlMappingGraph,blankLogicalSource,
                RMLVocabulary.RML_NAMESPACE, RMLVocabulary.RMLTerm.SPLIT, triplesMap);
        
        //TODO: add separate function for all conditions extraction both for Term Maps and Logical Source
        Set<EqualCondition> equalCondition = EqualConditionExtractor.extractEqualCondition(
                    rmlMappingGraph, blankLogicalSource);
        Set<ProcessCondition> processCondition = ProcessConditionExtractor.extractProcessCondition(
                    rmlMappingGraph, blankLogicalSource);
        Set<SplitCondition> splitCondition = SplitConditionExtractor.extractSplitCondition(
                    rmlMappingGraph, blankLogicalSource);
        Set<BindCondition> bindCondition = BindConditionExtractor.extractBindCondition(
                    rmlMappingGraph, blankLogicalSource);

        LogicalSource logicalSource = null;

        if (!sourceStatements.isEmpty()) {
            //Extract the file identifier
            for (Statement sourceStatement : sourceStatements) {
                String source ;
                //string input
                if(sourceStatement.getObject().getClass().getSimpleName().equals("MemLiteral"))
                    source = sourceStatement.getObject().stringValue();
                //object input
                else{
                    ConcreteInputFactory inputFactory = new ConcreteInputFactory();
                    source = inputFactory.chooseInput(
                            rmlMappingGraph, (Resource) sourceStatement.getObject());
                }
                
                if (!iterators.isEmpty()) {
                    if(!splitStatements.isEmpty())
                        logicalSource =
                            new StdLogicalSource(
                            iterators.get(0).getObject().stringValue(), source.toString(), 
                                referenceFormulation, splitStatements.get(0).getObject().stringValue());
                    else if(equalCondition != null || processCondition != null
                            || splitCondition != null || bindCondition != null ){
                        logicalSource = 
                            new StdLogicalSource(
                            iterators.get(0).getObject().stringValue(), 
                                source.toString(), referenceFormulation,
                                equalCondition, processCondition, splitCondition, bindCondition);
                    }
                    else{
                    logicalSource =
                            new StdLogicalSource(
                            iterators.get(0).getObject().stringValue(), 
                            source.toString(), referenceFormulation);
                    }
                }
                else
                    if(!splitStatements.isEmpty())
                        logicalSource =
                            new StdLogicalSource(
                            iterators.get(0).getObject().stringValue(), source.toString(), 
                                referenceFormulation, splitStatements.get(0).getObject().toString());
                    else
                        logicalSource = new StdLogicalSource(source.toString(), referenceFormulation);
                    
            }
        }
        
        log.debug(
                Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                + "Logical source extracted : "
                + logicalSource);
        return logicalSource;
    }
    
    /*private String chooseInput(RMLSesameDataSet rmlMappingGraph, Resource resource){
        InputExtractor input ;
        String inputSource = null;
        
        List<Statement> inputStatement = rmlMappingGraph.tuplePattern(
                        (Resource) resource, RDF.TYPE, null);
        log.error("hydra " + HydraVocabulary.HYDRA_NAMESPACE + HydraVocabulary.HydraTerm.API_DOCUMENTATION_CLASS);
        switch(inputStatement.get(0).getObject().stringValue().toString()){
            case ("http://www.w3.org/ns/hydra/core#APIDocumentation"):
            //case (HydraVocabulary.HYDRA_NAMESPACE + HydraVocabulary.HydraTerm.API_DOCUMENTATION_CLASS):
                input = new ApiExtractor();
                inputSource = input.extractInput(rmlMappingGraph, resource);
                break;
            case ("http://www.w3.org/ns/sparql-service-description#Service"):
                break;
            case("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#Database"):
                break;
            default:
                log.error("Not identified input");
        }
        
        return inputSource;
        
    }*/
    
    protected Resource extractLogicalSource(
            RMLSesameDataSet rmlMappingGraph, Resource triplesMapSubject, TriplesMap triplesMap) {

        List<Statement> logicalSourceStatements = getStatements(
                rmlMappingGraph, triplesMapSubject,
                RMLVocabulary.RML_NAMESPACE, RMLVocabulary.RMLTerm.LOGICAL_SOURCE, triplesMap);
        
        Resource blankLogicalSource = null;
        if (!logicalSourceStatements.isEmpty())
            blankLogicalSource = (Resource) logicalSourceStatements.get(0).getObject();
            //TODO:Check if I need to add another control here
                
        return blankLogicalSource;
    }
       
    protected QLVocabulary.QLTerm getReferenceFormulation(
            RMLSesameDataSet rmlMappingGraph, Resource triplesMapSubject, 
            Resource subject, TriplesMap triplesMap) 
    {       
        List<Statement> statements = getStatements(
                rmlMappingGraph, subject, 
                RMLVocabulary.RML_NAMESPACE, RMLVocabulary.RMLTerm.REFERENCE_FORMULATION, triplesMap);
        
        if (statements.isEmpty()) 
            return null;
        else
            return QLVocabulary.getQLTerms(statements.get(0).getObject().stringValue());
        }
    
    protected SubjectMap extractSubjectMap(
            RMLSesameDataSet rmlMappingGraph, Resource triplesMapSubject,
            Set<GraphMap> savedGraphMaps, TriplesMap triplesMap){
        log.debug(
                Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                + "Extract subject map...");
        
        // Extract subject map
        List<Statement> statements = getStatements(rmlMappingGraph, triplesMapSubject,
                R2RMLVocabulary.R2RML_NAMESPACE, R2RMLVocabulary.R2RMLTerm.SUBJECT_MAP, triplesMap);
        
        Resource subjectMap ; 

        if(statements != null && statements.size() > 0)
            subjectMap = (Resource) statements.get(0).getObject();
        else
            return null;
        
        log.debug(
                Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                + "Found subject map : "
                + subjectMap.stringValue());

        Value constantValue = extractValueFromTermMap(rmlMappingGraph,
                subjectMap, R2RMLVocabulary.R2RMLTerm.CONSTANT, triplesMap);
        String stringTemplate = extractLiteralFromTermMap(rmlMappingGraph,
                subjectMap, R2RMLVocabulary.R2RMLTerm.TEMPLATE, triplesMap);
        URI termType = (URI) extractValueFromTermMap(rmlMappingGraph,
                subjectMap, R2RMLVocabulary.R2RMLTerm.TERM_TYPE, triplesMap);
        String inverseExpression = extractLiteralFromTermMap(rmlMappingGraph,
                subjectMap, R2RMLVocabulary.R2RMLTerm.INVERSE_EXPRESSION, triplesMap);
        //TODO:fix the following validation
        //validator.checkTermMap(constantValue, stringTemplate, null, subjectMap.toString());
        //TODO:handle the folowings separately
        String split = extractLiteralFromTermMap(rmlMappingGraph,
                subjectMap, RMLVocabulary.RMLTerm.SPLIT, triplesMap);
        String process = extractLiteralFromTermMap(rmlMappingGraph,
                subjectMap, RMLVocabulary.RMLTerm.PROCESS, triplesMap);
        String replace = extractLiteralFromTermMap(rmlMappingGraph,
                subjectMap, RMLVocabulary.RMLTerm.REPLACE, triplesMap);

        //TODO:Add check if the referenceValue is a valid reference according to the reference formulation
        ReferenceIdentifier referenceValue = 
                extractReferenceIdentifier(rmlMappingGraph, subjectMap, triplesMap);
        Set<EqualCondition> equalCondition = EqualConditionExtractor.extractEqualCondition(
                    rmlMappingGraph, subjectMap);
        Set<ProcessCondition> processCondition = ProcessConditionExtractor.extractProcessCondition(
                    rmlMappingGraph, subjectMap);
        Set<SplitCondition> splitCondition = SplitConditionExtractor.extractSplitCondition(
                    rmlMappingGraph, subjectMap);
        Set<BindCondition> bindCondition = BindConditionExtractor.extractBindCondition(
                    rmlMappingGraph, subjectMap);
        
        //AD: The values of the rr:class property must be IRIs. 
        //AD: Would that mean that it can not be a reference to an extract of the input or a template?
        Set<URI> classIRIs = extractURIsFromTermMap(rmlMappingGraph,
                subjectMap, R2RMLVocabulary.R2RMLTerm.CLASS);
        
        //AD:Move it a separate function that extracts the GraphMaps
        Set<GraphMap> graphMaps = new HashSet<GraphMap>();
        Set<Value> graphMapValues = extractValuesFromResource(
                rmlMappingGraph, subjectMap, R2RMLVocabulary.R2RMLTerm.GRAPH_MAP);
       
        if (graphMapValues != null) {
            graphMaps = extractGraphMapValues(rmlMappingGraph, graphMapValues, savedGraphMaps, triplesMap);
            log.info(
                    Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "graph Maps returned " + graphMaps);
        }
        
        SubjectMap result = null;
        try {
            result = new StdSubjectMap(triplesMap, constantValue,
                    stringTemplate, termType, inverseExpression,
                    referenceValue, classIRIs, graphMaps, split, process, replace,
                    equalCondition, processCondition, splitCondition, bindCondition);

        } catch (R2RMLDataError ex) {
            log.error(ex);
            java.util.logging.Logger.getLogger(RMLUnValidatedMappingExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidR2RMLStructureException ex) {
            log.error(ex);
            java.util.logging.Logger.getLogger(RMLUnValidatedMappingExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidR2RMLSyntaxException ex) {
            log.error(ex);
            java.util.logging.Logger.getLogger(RMLUnValidatedMappingExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        log.debug(
                Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                + "Subject map extracted.");
        return result;
    }
    
    @Override
    public Set<PredicateObjectMap> extractPredicateObjectMaps(
            RMLSesameDataSet rmlMappingGraph, Resource triplesMapSubject,
            Set<GraphMap> graphMaps, TriplesMap result,
            Map<Resource, TriplesMap> triplesMapResources)  {
        // Extract predicate-object maps
        URI p = rmlMappingGraph.URIref(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.PREDICATE_OBJECT_MAP);
        
        List<Statement> statements = rmlMappingGraph.tuplePattern(
                triplesMapSubject, p, null);
        
        Set<PredicateObjectMap> predicateObjectMaps = new HashSet<PredicateObjectMap>();
        try {
            for (Statement statement : statements) {
                PredicateObjectMap predicateObjectMap = extractPredicateObjectMap(       
                        rmlMappingGraph, triplesMapSubject,
                        (Resource) statement.getObject(),
                        graphMaps, triplesMapResources, result);
                if (predicateObjectMap != null) {
                    predicateObjectMap.setOwnTriplesMap(result);
                    predicateObjectMaps.add(predicateObjectMap);
                }
            }
        } catch (ClassCastException e) {
            log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    +  "A resource was expected in object of predicateObjectMap of "
                    + triplesMapSubject.stringValue());
        }
        log.debug(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "extractPredicateObjectMaps] Number of extracted predicate-object maps : "
                + predicateObjectMaps.size());
        return predicateObjectMaps;
    }
      
    //@Override
    public PredicateObjectMap extractPredicateObjectMap(
            RMLSesameDataSet rmlMappingGraph,
            Resource triplesMapSubject,
            Resource predicateObject,
            Set<GraphMap> savedGraphMaps,
            Map<Resource, TriplesMap> triplesMapResources,
            TriplesMap triplesMap) {
        URI p = rmlMappingGraph.URIref(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.PREDICATE_MAP);
        
        List<Statement> predicate_statements = rmlMappingGraph.tuplePattern(
                predicateObject, p, null);
        Set<PredicateMap> predicateMaps = new HashSet<PredicateMap>();
        for (Statement predicate_statement : predicate_statements) {
            PredicateMap predicateMap = extractPredicateMap(
                    rmlMappingGraph, predicate_statement,
                    savedGraphMaps, triplesMap);
            predicateMaps.add(predicateMap);
        }
        // Extract object maps
        URI o = rmlMappingGraph.URIref(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.OBJECT_MAP);
        List<Statement> object_statements = rmlMappingGraph.tuplePattern(predicateObject, o, null);

        /*TODO:move the following to the validated extractor
         * if (object_statements.size() < 1) {
         log.debug(
         Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
         + predicateObject.stringValue()
         + " has no object map defined : one or more is required.");
         }*/
        Set<ObjectMap> objectMaps = new HashSet<ObjectMap>();
        Set<ReferencingObjectMap> refObjectMaps = new HashSet<ReferencingObjectMap>();
        for (Statement object_statement : object_statements) {
            refObjectMaps = processReferencingObjectMap(
                    rmlMappingGraph, object_statements, savedGraphMaps,
                    triplesMapResources, triplesMap, triplesMapSubject, predicateObject);
            if (refObjectMaps.isEmpty()) {
                ObjectMap objectMap = extractObjectMap(rmlMappingGraph,
                        (Resource) object_statement.getObject(), savedGraphMaps, triplesMap);
                try {
                    objectMap.setOwnTriplesMap(triplesMapResources.get(triplesMapSubject));
                } catch (InvalidR2RMLStructureException ex) {
                    java.util.logging.Logger.getLogger(RMLUnValidatedMappingExtractor.class.getName()).log(Level.SEVERE, null, ex);
                }
                objectMaps.add(objectMap);
            }
            PredicateObjectMap predicateObjectMap = new StdPredicateObjectMap(
                    predicateMaps, objectMaps, refObjectMaps);

            processGraphMaps(rmlMappingGraph, predicateObject, triplesMap, predicateObjectMap, savedGraphMaps);

            log.debug(
                    Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "Extract predicate-object map done.");
            return predicateObjectMap;
        }

        return null;
    }
    
    private Set<ReferencingObjectMap> processReferencingObjectMap(
            RMLSesameDataSet rmlMappingGraph, List<Statement> object_statements, Set<GraphMap> savedGraphMaps,
            Map<Resource, TriplesMap> triplesMapResources, TriplesMap triplesMap, Resource triplesMapSubject, Resource predicateObject) {
        Set<ReferencingObjectMap> refObjectMaps = new HashSet<ReferencingObjectMap>();
        try {
            //for (Statement statement : statements) {
            log.debug(
                    Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "Try to extract object map..");
            ReferencingObjectMap refObjectMap = extractReferencingObjectMap(
                    rmlMappingGraph, (Resource) object_statements.get(0).getObject(),
                    savedGraphMaps, triplesMapResources, triplesMap);
            if (refObjectMap != null) {
                //refObjectMap.setOwnTriplesMap(triplesMapResources.get(triplesMapSubject));
                refObjectMaps.add(refObjectMap);
            }
            //} 
        } catch (ClassCastException e) {
            log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    +  "A resource was expected in object of objectMap of "
                    + predicateObject.stringValue());
        } 
        return refObjectMaps;
    }
    
    private PredicateObjectMap processGraphMaps(
            RMLSesameDataSet rmlMappingGraph, Resource predicateObject, TriplesMap triplesMap, 
            PredicateObjectMap predicateObjectMap, Set<GraphMap> savedGraphMaps) {
        // Add graphMaps
        Set<GraphMap> graphMaps = new HashSet<GraphMap>();
        Set<Value> graphMapValues = extractValuesFromResource(
                rmlMappingGraph, predicateObject, R2RMLVocabulary.R2RMLTerm.GRAPH_MAP);

        if (graphMapValues != null) {
            graphMaps = extractGraphMapValues(
                    rmlMappingGraph, graphMapValues, savedGraphMaps, triplesMap);
            log.info(
                    Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "graph Maps returned " + graphMaps);
        }

        predicateObjectMap.setGraphMaps(graphMaps);
        return predicateObjectMap;
    }
    
    @Override
    public PredicateMap extractPredicateMap(
            RMLSesameDataSet rmlMappingGraph, Statement statement,
            Set<GraphMap> graphMaps, TriplesMap triplesMap) {
        Resource object = (Resource) statement.getObject();
        try {
            // Extract object maps properties
            Value constantValue = extractValueFromTermMap(rmlMappingGraph,
                    object, R2RMLVocabulary.R2RMLTerm.CONSTANT, triplesMap);
            String stringTemplate = extractLiteralFromTermMap(rmlMappingGraph,
                    object, R2RMLVocabulary.R2RMLTerm.TEMPLATE, triplesMap);
            URI termType = (URI) extractValueFromTermMap(rmlMappingGraph, object,
                    R2RMLVocabulary.R2RMLTerm.TERM_TYPE, triplesMap);

            String inverseExpression = extractLiteralFromTermMap(rmlMappingGraph,
                    object, R2RMLVocabulary.R2RMLTerm.INVERSE_EXPRESSION, triplesMap);

            //MVS: Decide on ReferenceIdentifier
            ReferenceIdentifier referenceValue = 
                    extractReferenceIdentifier(rmlMappingGraph, object, triplesMap);

            PredicateMap result = new StdPredicateMap(null, constantValue,
                    stringTemplate, inverseExpression, referenceValue, termType);
            log.debug(
                    Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "Extract predicate map done.");
            return result;
        } catch (R2RMLDataError ex) {
            java.util.logging.Logger.getLogger(RMLUnValidatedMappingExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidR2RMLStructureException ex) {
            java.util.logging.Logger.getLogger(RMLUnValidatedMappingExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidR2RMLSyntaxException ex) {
            java.util.logging.Logger.getLogger(RMLUnValidatedMappingExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    protected ReferencingObjectMap extractReferencingObjectMap(
            RMLSesameDataSet rmlMappingGraph, Resource object,
            Set<GraphMap> graphMaps,
            Map<Resource, TriplesMap> triplesMapResources, TriplesMap triplesMap){
        try {
            URI parentTriplesMap = (URI) extractValueFromTermMap(rmlMappingGraph,
                    object, R2RMLVocabulary.R2RMLTerm.PARENT_TRIPLES_MAP, triplesMap);
            Set<JoinCondition> joinConditions = extractJoinConditions(
                    rmlMappingGraph, object, triplesMap);
            
            Set<EqualCondition> equalConditions = EqualConditionExtractor.extractEqualCondition(
                    rmlMappingGraph, object);
            Set<ProcessCondition> processConditions = ProcessConditionExtractor.extractProcessCondition(
                    rmlMappingGraph, object);
            Set<SplitCondition> splitConditions = SplitConditionExtractor.extractSplitCondition(
                    rmlMappingGraph, object);
            Set<BindCondition> bindConditions = BindConditionExtractor.extractBindCondition(
                    rmlMappingGraph, object);
            
            if (parentTriplesMap == null && !joinConditions.isEmpty()) {
                log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                        + object.stringValue()
                        + " has no parentTriplesMap map defined whereas one or more joinConditions exist"
                        + " : exactly one parentTripleMap is required.");
            }
            if (parentTriplesMap == null && joinConditions.isEmpty()) {
                return null;
            }
            // Extract parent
            boolean contains = false;
            TriplesMap parent = null;
            for (Resource triplesMapResource : triplesMapResources.keySet()) {
                if (triplesMapResource.stringValue().equals(
                        parentTriplesMap.stringValue())) {
                    contains = true;
                    parent = triplesMapResources.get(triplesMapResource);
                    log.debug(
                            Thread.currentThread().getStackTrace()[1].getMethodName() + ": " 
                            + "Parent triples map found : "
                            + triplesMapResource.stringValue());
                    break;
                }
            }
            if (!contains) {
                log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                        +  object.stringValue()
                        + " reference to parent triples maps is broken : "
                        + parentTriplesMap.stringValue() + " not found.");
            }
            // Link between this reerencing object and its triplesMap parent will be
            // performed
            // at the end f treatment.
            ReferencingObjectMap refObjectMap = new StdReferencingObjectMap(null,
                    parent, joinConditions, equalConditions, processConditions,
                    splitConditions, bindConditions);
            log.debug(
                    Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "Extract referencing object map done.");
            return refObjectMap;
        } catch (InvalidR2RMLStructureException ex) {
            java.util.logging.Logger.getLogger(RMLUnValidatedMappingExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    @Override
    public ObjectMap extractObjectMap(RMLSesameDataSet rmlMappingGraph,
            Resource object, Set<GraphMap> graphMaps, TriplesMap triplesMap){
        try {
            log.debug(
                    Thread.currentThread().getStackTrace()[1].getMethodName() + ": " 
                    + "Extract object map..");
            // Extract object maps properties
            Value constantValue = extractValueFromTermMap(rmlMappingGraph,
                    object, R2RMLVocabulary.R2RMLTerm.CONSTANT, triplesMap);
            String stringTemplate = extractLiteralFromTermMap(rmlMappingGraph,
                    object, R2RMLVocabulary.R2RMLTerm.TEMPLATE, triplesMap);
            String languageTag = extractLiteralFromTermMap(rmlMappingGraph,
                    object, R2RMLVocabulary.R2RMLTerm.LANGUAGE, triplesMap);
            URI termType = (URI) extractValueFromTermMap(rmlMappingGraph, object,
                    R2RMLVocabulary.R2RMLTerm.TERM_TYPE, triplesMap);
            URI dataType = (URI) extractValueFromTermMap(rmlMappingGraph, object,
                    R2RMLVocabulary.R2RMLTerm.DATATYPE, triplesMap);
            String inverseExpression = extractLiteralFromTermMap(rmlMappingGraph,
                    object, R2RMLVocabulary.R2RMLTerm.INVERSE_EXPRESSION, triplesMap);
            //TODO:handle the folowings separately
            String split = extractLiteralFromTermMap(rmlMappingGraph,
                    object, RMLVocabulary.RMLTerm.SPLIT, triplesMap);
            String process = extractLiteralFromTermMap(rmlMappingGraph,
                    object, RMLVocabulary.RMLTerm.PROCESS, triplesMap);
            String replace = extractLiteralFromTermMap(rmlMappingGraph,
                    object, RMLVocabulary.RMLTerm.REPLACE, triplesMap);
            Set<EqualCondition> equalCondition = EqualConditionExtractor.extractEqualCondition(
                    rmlMappingGraph, object);
            Set<ProcessCondition> processCondition = ProcessConditionExtractor.extractProcessCondition(
                    rmlMappingGraph, object);
            Set<SplitCondition> splitCondition = SplitConditionExtractor.extractSplitCondition(
                    rmlMappingGraph, object);
            Set<BindCondition> bindCondition = BindConditionExtractor.extractBindCondition(
                    rmlMappingGraph, object);
            
            //MVS: Decide on ReferenceIdentifier
            ReferenceIdentifier referenceValue = 
                    extractReferenceIdentifier(rmlMappingGraph, object, triplesMap);
            //TODO:add the following validator
            //validator.checkTermMap(constantValue, stringTemplate, referenceValue, o.stringValue());

            StdObjectMap result = new StdObjectMap(null, constantValue, dataType,
                    languageTag, stringTemplate, termType, inverseExpression,
                    referenceValue, split, process, replace, 
                    equalCondition, processCondition, splitCondition, bindCondition);

            return result;
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(RMLUnValidatedMappingExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return null;
    }
    
    protected Value extractValueFromTermMap(
            RMLSesameDataSet rmlMappingGraph, Resource termType,
            Enum term, TriplesMap triplesMap) {
        
        List<Statement> statements = 
                getStatements(rmlMappingGraph, term,  termType, triplesMap);
        
        if (statements.isEmpty()) 
            return null;
        else{
            log.debug(
                Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                + "Extracted "
                + term + " : " + statements.get(0).getObject().stringValue());
            return statements.get(0).getObject();
        }
        
    }
    
    private Set<JoinCondition> extractJoinConditions(
            RMLSesameDataSet rmlMappingGraph, Resource object, TriplesMap triplesMap){
        log.debug(
                Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                + "Extract join conditions..");
        Set<JoinCondition> result = new HashSet<JoinCondition>();
        // Extract predicate-object maps
        URI p = rmlMappingGraph.URIref(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.JOIN_CONDITION);
        List<Statement> statements = rmlMappingGraph.tuplePattern(object, p, null);
        try {
            for (Statement statement : statements) {
                Resource jc = (Resource) statement.getObject();
                String child = extractLiteralFromTermMap(rmlMappingGraph, jc,
                        R2RMLVocabulary.R2RMLTerm.CHILD, triplesMap);
                String parent = extractLiteralFromTermMap(rmlMappingGraph,
                        jc, R2RMLVocabulary.R2RMLTerm.PARENT, triplesMap);
                if (parent == null || child == null) {
                    log.error(
                            Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                            +  object.stringValue()
                            + " must have exactly two properties child and parent. ");
                }
                try {
                    result.add(new StdJoinCondition(child, parent));
                } catch (InvalidR2RMLStructureException ex) {
                    java.util.logging.Logger.getLogger(RMLUnValidatedMappingExtractor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvalidR2RMLSyntaxException ex) {
                    java.util.logging.Logger.getLogger(RMLUnValidatedMappingExtractor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (ClassCastException e) {
            log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "A resource was expected in object of predicateMap of "
                    + object.stringValue());
        } 
        log.debug(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + " Extract join conditions done.");
        return result;
    }
    
    protected String extractLiteralFromTermMap(
            RMLSesameDataSet rmlMappingGraph, Resource termType, Enum term, TriplesMap triplesMap){

        List<Statement> statements = 
                getStatements(rmlMappingGraph, term,  termType, triplesMap);
        
        if (statements.isEmpty()) 
            return null;
        else {
            String result = statements.get(0).getObject().stringValue();
            if (log.isDebugEnabled()) 
                log.debug(
                        Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                        + "Extracted "
                        + term + " : " + result);
            return result;
        }
    }
    
     protected ReferenceIdentifier extractReferenceIdentifier(
            RMLSesameDataSet rmlMappingGraph, Resource resource, TriplesMap triplesMap) {

        String columnValueStr = extractLiteralFromTermMap(
                rmlMappingGraph, resource, R2RMLVocabulary.R2RMLTerm.COLUMN, triplesMap);
        String referenceValueStr = extractLiteralFromTermMap(
                rmlMappingGraph, resource, RMLVocabulary.RMLTerm.REFERENCE, triplesMap);

        if (columnValueStr != null && referenceValueStr != null) {
            log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + resource
                    + " has a reference and column defined.");
        }

        if (columnValueStr != null) {
            return ReferenceIdentifierImpl.buildFromR2RMLConfigFile(columnValueStr);
        }

        return ReferenceIdentifierImpl.buildFromR2RMLConfigFile(referenceValueStr);
    }
     
    protected static Set<URI> extractURIsFromTermMap(
            RMLSesameDataSet rmlMappingGraph, Resource termType,
            R2RMLVocabulary.R2RMLTerm term){
            
        URI p = getTermURI(rmlMappingGraph, term);

        List<Statement> statements = rmlMappingGraph.tuplePattern(termType,
                p, null);
        if (statements.isEmpty()) {
            return null;
        }
        Set<URI> uris = new HashSet<URI>();
        for (Statement statement : statements) {
            URI uri = (URI) statement.getObject();
            log.debug(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + term + " : " + uri.stringValue());
            uris.add(uri);
        }
        return uris;
    } 
    
    protected static Set<Value> extractValuesFromResource(
            RMLSesameDataSet rmlMappingGraph,
            Resource termType,
            Enum term){
            
        URI p = getTermURI(rmlMappingGraph, term);

        List<Statement> statements = rmlMappingGraph.tuplePattern(termType,
                p, null);
        if (statements.isEmpty()) {
            return null;
        }
        Set<Value> values = new HashSet<Value>();
        for (Statement statement : statements) {
            Value value = statement.getObject();
            log.debug(
                    Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "Extracted "
                    + term + " : " + value.stringValue());
            values.add(value);
        }
        return values;
    }
    
    private Set<GraphMap> extractGraphMapValues(
            RMLSesameDataSet rmlMappingGraph, Set<Value> graphMapValues, 
            Set<GraphMap> savedGraphMaps, TriplesMap triplesMap) {
        
        Set<GraphMap> graphMaps = new HashSet<GraphMap>();
        
            for (Value graphMap : graphMapValues) {
                // Create associated graphMap if it has not already created
                boolean found = false;
                GraphMap graphMapFound = null;
                
                if (found) {
                    graphMaps.add(graphMapFound);
                } else {
                    GraphMap newGraphMap = null;
                    newGraphMap = extractGraphMap(rmlMappingGraph, (Resource) graphMap, triplesMap);
                    
                    savedGraphMaps.add(newGraphMap);
                    graphMaps.add(newGraphMap);
                }
            }
        
        return graphMaps;
    }
    
    protected GraphMap extractGraphMap(
            RMLSesameDataSet rmlMappingGraph,
            Resource graphMap, TriplesMap triplesMap) {
        log.debug(
                Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                + "Extract graph map...");

        Value constantValue = extractValueFromTermMap(rmlMappingGraph,
                graphMap, R2RMLVocabulary.R2RMLTerm.CONSTANT, triplesMap);
        String stringTemplate = extractLiteralFromTermMap(rmlMappingGraph,
                graphMap, R2RMLVocabulary.R2RMLTerm.TEMPLATE, triplesMap);
        String inverseExpression = extractLiteralFromTermMap(rmlMappingGraph,
                graphMap, R2RMLVocabulary.R2RMLTerm.INVERSE_EXPRESSION, triplesMap);

        ReferenceIdentifier referenceValue = 
                extractReferenceIdentifier(rmlMappingGraph, graphMap, triplesMap);

        URI termType = (URI) extractValueFromTermMap(rmlMappingGraph,
                graphMap, R2RMLVocabulary.R2RMLTerm.TERM_TYPE, triplesMap);

        GraphMap result = null;
        try {
            result = new StdGraphMap(constantValue, stringTemplate,
           inverseExpression, referenceValue, termType);
        } catch (R2RMLDataError ex) {
            java.util.logging.Logger.getLogger(RMLUnValidatedMappingExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidR2RMLStructureException ex) {
            java.util.logging.Logger.getLogger(RMLUnValidatedMappingExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidR2RMLSyntaxException ex) {
            java.util.logging.Logger.getLogger(RMLUnValidatedMappingExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        log.debug(
                Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                + "Graph map extracted.");
        return result;
    }
    
    protected static URI getTermURI(
            RMLSesameDataSet rmlMappingGraph, Enum term) {
        String namespace = R2RMLVocabulary.R2RML_NAMESPACE;

        if (term instanceof RMLVocabulary.RMLTerm) {
            namespace = RMLVocabulary.RML_NAMESPACE;
        } else if (!(term instanceof R2RMLVocabulary.R2RMLTerm)) 
            log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + term + " is not valid.");

        return rmlMappingGraph
                .URIref(namespace + term);
    }
    
    protected List<Statement> getStatements(
            RMLSesameDataSet rmlMappingGraph, Resource triplesMapSubject, 
            String namespace, Term term, TriplesMap triplesMap){
        URI logicalSource = rmlMappingGraph.URIref(namespace
                + term);
        List<Statement> source = rmlMappingGraph.tuplePattern(
                triplesMapSubject, logicalSource, null);
        
        return source;
    }
    
    protected List<Statement> getStatements(
            RMLSesameDataSet rmlMappingGraph, Enum term,  Resource termType, TriplesMap triplesMap){
        URI p = getTermURI(rmlMappingGraph, term);

        List<Statement> statements = rmlMappingGraph.tuplePattern(termType,
                p, null);
        
        return statements;
    }

}