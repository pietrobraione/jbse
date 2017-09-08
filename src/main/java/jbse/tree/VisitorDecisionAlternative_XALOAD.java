package jbse.tree;

/**
 * Visitor interface for {@link DecisionAlternative_XALOAD}s.
 * 
 * @author Pietro Braione
 */
public interface VisitorDecisionAlternative_XALOAD {
	void visitDecisionAlternative_XALOAD_Out(DecisionAlternative_XALOAD_Out dao) throws Exception;
    void visitDecisionAlternative_XALOAD_Aliases(DecisionAlternative_XALOAD_Aliases dai) throws Exception;
	void visitDecisionAlternative_XALOAD_Expands(DecisionAlternative_XALOAD_Expands dac) throws Exception;
	void visitDecisionAlternative_XALOAD_Null(DecisionAlternative_XALOAD_Null dan) throws Exception;
	void visitDecisionAlternative_XALOAD_Resolved(DecisionAlternative_XALOAD_Resolved dav) throws Exception;
}
