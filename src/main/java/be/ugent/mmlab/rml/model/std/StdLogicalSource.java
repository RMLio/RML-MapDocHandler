package be.ugent.mmlab.rml.model.std;

import be.ugent.mmlab.rml.model.LogicalSource;
import be.ugent.mmlab.rml.model.condition.BindCondition;
import be.ugent.mmlab.rml.model.condition.Condition;
import be.ugent.mmlab.rml.model.condition.EqualCondition;
import be.ugent.mmlab.rml.model.condition.ProcessCondition;
import be.ugent.mmlab.rml.model.condition.SplitCondition;
import be.ugent.mmlab.rml.vocabulary.RMLVocabulary.QLTerm;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *  Concrete implementation of a Logical Source
 * 
 * @author mielvandersande, andimou
 */
public class StdLogicalSource implements LogicalSource {
    
    private static Log log = LogFactory.getLog(StdLogicalSource.class);

    private String iterator;
    private QLTerm referenceFormulation = QLTerm.SQL_CLASS;
    private String identifier;
    private String splitCondition;
    
    private Set<EqualCondition>     equalConditions;
    private Set<ProcessCondition>   processConditions;
    private Set<SplitCondition>     splitConditions;
    private Set<BindCondition>      bindConditions;

    public StdLogicalSource(String identifier, QLTerm referenceFormulation) {
        this.referenceFormulation = referenceFormulation;
        this.identifier = identifier;
    }
    
    public StdLogicalSource(String identifier, QLTerm referenceFormulation, String splitCondition) {
        this.referenceFormulation = referenceFormulation;
        this.identifier = identifier;
        this.splitCondition = splitCondition;
    }

    public StdLogicalSource(String reference) {
        this.iterator = reference;
    }

    public StdLogicalSource(String iterator, String identifier, QLTerm referenceFormulation) {
        this.iterator = iterator;
        this.identifier = identifier;
        this.referenceFormulation = referenceFormulation;
    }
    
    public StdLogicalSource(String iterator, String identifier, 
            QLTerm referenceFormulation, String splitCondition) {
        this.iterator = iterator;
        this.identifier = identifier;
        this.referenceFormulation = referenceFormulation;
        this.splitCondition = splitCondition;
    }
    
    public StdLogicalSource(String iterator, String identifier, QLTerm referenceFormulation,
            Set<EqualCondition> equalCondition, Set<ProcessCondition> processCondition,
            Set<SplitCondition> splitCondition, Set<BindCondition> bindCondition) {
        this.iterator = iterator;
        this.identifier = identifier;
        this.referenceFormulation = referenceFormulation;
        setEqualConditions(equalCondition);
        setProcessConditions(processCondition);
        setSplitConditions(splitCondition);
        setBindConditions(bindCondition);
    }
    
    @Override
    public String getReference() {
        return iterator;
    }

    @Override
    public QLTerm getReferenceFormulation() {
        return referenceFormulation;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return "[StdLogicalSource : iterator = " + iterator
                + "; identifier" + identifier + "; referenceFormulation = " + referenceFormulation 
                + "; splitCondition = " + splitCondition + "]";
    }
    
    @Override
    public String getSplitCondition() {
        return splitCondition;
    }
    
    private void setEqualConditions(Set<EqualCondition> equalConditions) {
            this.equalConditions = new HashSet<EqualCondition>();
            this.equalConditions.addAll(equalConditions);
        }

        private void setProcessConditions(Set<ProcessCondition> processConditions) {
            this.processConditions = new HashSet<ProcessCondition>();
            this.processConditions.addAll(processConditions);
        }

        private void setSplitConditions(Set<SplitCondition> splitConditions) {
            this.splitConditions = new HashSet<SplitCondition>();
            this.splitConditions.addAll(splitConditions);
        }
        
        private void setBindConditions(Set<BindCondition> bindConditions) {
            this.bindConditions = new HashSet<BindCondition>();
            this.bindConditions.addAll(bindConditions);
        }

    /**
     *
     * @return
     */
    @Override
    public Set<EqualCondition> getEqualConditions() {
        return this.equalConditions;
    }

    /**
     *
     * @return this.processConditions
     */
    @Override
    public Set<ProcessCondition> getProcessConditions() {
        return this.processConditions;
    }

    /**
     *
     * @return this.splitConditions
     */
    @Override
    public Set<SplitCondition> getSplitConditions() {
        return this.splitConditions;
    }

    /**
     *
     * @return
     */
    @Override
    public Set<BindCondition> getBindConditions() {
        return this.bindConditions;
    }
    
    @Override
    public Set<Condition> getConditions() {
        Set<Condition> conditions = new HashSet<Condition>();
        if (this.equalConditions != null) {
            conditions.addAll(this.equalConditions);
        } else if (this.processConditions != null) {
            conditions.addAll(this.processConditions);
        } else if (this.splitConditions != null) {
            conditions.addAll(this.splitConditions);
        } else if (this.bindConditions != null) {
            conditions.addAll(this.bindConditions);
        }
        return conditions;
    }
}