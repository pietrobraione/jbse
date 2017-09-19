package jbse.tree;

/**
 * Visitor interface for {@link DecisionAlternative_XLOAD_GETX}s.
 * 
 * @author Pietro Braione
 */
public interface VisitorDecisionAlternative_XLOAD_GETX {
    void visitDecisionAlternative_XLOAD_GETX_Aliases(DecisionAlternative_XLOAD_GETX_Aliases dro) throws Exception;
	void visitDecisionAlternative_XLOAD_GETX_Expands(DecisionAlternative_XLOAD_GETX_Expands drc) throws Exception;
    void visitDecisionAlternative_XLOAD_GETX_Null(DecisionAlternative_XLOAD_GETX_Null drn) throws Exception;
	void visitDecisionAlternative_XLOAD_GETX_Resolved(DecisionAlternative_XLOAD_GETX_Resolved drr) throws Exception;
}
