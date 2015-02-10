package jbse.tree;

/**
 * Visitor interface for {@link DecisionAlternative_XLOAD_GETX}s.
 * 
 * @author Pietro Braione
 */
public interface VisitorDecisionAlternative_XLOAD_GETX {
    void visitDecisionAlternative_XLOAD_GETX_RefAliases(DecisionAlternative_XLOAD_GETX_RefAliases dro) throws Exception;
	void visitDecisionAlternative_XLOAD_GETX_RefExpands(DecisionAlternative_XLOAD_GETX_RefExpands drc) throws Exception;
    void visitDecisionAlternative_XLOAD_GETX_RefNull(DecisionAlternative_XLOAD_GETX_RefNull drn) throws Exception;
	void visitDecisionAlternative_XLOAD_GETX_Resolved(DecisionAlternative_XLOAD_GETX_Resolved drr) throws Exception;
}
