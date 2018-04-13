/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : RML Mapping Factory
 *
 * Factory responsible of RML Mapping generation.
 *
 * @author andimou
 *
 ***************************************************************************
 */
package be.ugent.mmlab.rml.mapdochandler.extraction;

import be.ugent.mmlab.rml.model.RMLMapping;


public interface RMLMappingFactory {

    

    /**
     * Extract RML Mapping object from a RML file written with Turtle syntax.
     *
     * Important : The R2RML vocabulary also includes the following R2RML
     * classes, which represent various R2RML mapping constructs. Using these
     * classes is optional in a mapping graph. The applicable class of a
     * resource can always be inferred from its properties. Consequently, in
     * order to identify each triple type, a rule will be used to extract the
     * applicable class of a resource.
     *
     * @param fileToRMLFile
     * @return
     */
    public RMLMapping extractRMLMapping(String fileToRMLFile);

}
