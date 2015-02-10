package jbse.tree;

/**
 * Visitor interface for {@link DecisionAlternative_XALOAD}s.
 * 
 * @author Pietro Braione
 */
public interface VisitorDecisionAlternative_XALOAD {
	void visitDecisionAlternative_XALOAD_Out(DecisionAlternative_XALOAD_Out dao) throws Exception;
    void visitDecisionAlternative_XALOAD_RefAliases(DecisionAlternative_XALOAD_RefAliases dai) throws Exception;
	void visitDecisionAlternative_XALOAD_RefExpands(DecisionAlternative_XALOAD_RefExpands dac) throws Exception;
	void visitDecisionAlternative_XALOAD_RefNull(DecisionAlternative_XALOAD_RefNull dan) throws Exception;
	void visitDecisionAlternative_XALOAD_Resolved(DecisionAlternative_XALOAD_Resolved dav) throws Exception;
}
