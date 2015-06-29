/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : LogicalSourceExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */

package be.ugent.mmlab.rml.extraction.concrete;

import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.sesame.RMLSesameDataSet;
import be.ugent.mmlab.rml.vocabulary.QLVocabulary;
import be.ugent.mmlab.rml.vocabulary.RMLVocabulary;
import java.util.List;
import org.apache.log4j.LogManager;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;

public class LogicalSourceExtractor {
    
    // Log
    private static final org.apache.log4j.Logger log = LogManager.getLogger(LogicalSourceExtractor.class);

    public Resource extractLogicalSource(
            RMLSesameDataSet rmlMappingGraph, Resource triplesMapSubject, TriplesMap triplesMap) {

        List<Statement> logicalSourceStatements = rmlMappingGraph.tuplePattern(triplesMapSubject,
                RMLTermExtractor.getTermURI(rmlMappingGraph, RMLVocabulary.RMLTerm.LOGICAL_SOURCE), null);
        Resource blankLogicalSource = null;
        if (!logicalSourceStatements.isEmpty()) {
            blankLogicalSource = (Resource) logicalSourceStatements.get(0).getObject();
        }
        return blankLogicalSource;
    }

    /**
     *
     * @param rmlMappingGraph
     * @param triplesMapSubject
     * @param subject
     * @param triplesMap
     * @return
     */
    public QLVocabulary.QLTerm getReferenceFormulation(
            RMLSesameDataSet rmlMappingGraph, Resource triplesMapSubject,
            Resource subject, TriplesMap triplesMap) {
        URI logicalSource = rmlMappingGraph.URIref(
                RMLVocabulary.RML_NAMESPACE + RMLVocabulary.RMLTerm.REFERENCE_FORMULATION);

        List<Statement> statements = rmlMappingGraph.tuplePattern(subject, logicalSource, null);

        if (statements.isEmpty()) {
            return null;
        } else {
            return QLVocabulary.getQLTerms(statements.get(0).getObject().stringValue());
        }
    }
    
   public String getIterator(
            RMLSesameDataSet rmlMappingGraph, Resource triplesMapSubject,
            Resource subject, TriplesMap triplesMap) {
        URI logicalSource = rmlMappingGraph.URIref(
                RMLVocabulary.RML_NAMESPACE + RMLVocabulary.RMLTerm.ITERATOR);

        List<Statement> statements = rmlMappingGraph.tuplePattern(subject, logicalSource, null);

        if (statements.isEmpty()) {
            return null;
        } else {
            return statements.get(0).getObject().stringValue();
        }
    } 
}
