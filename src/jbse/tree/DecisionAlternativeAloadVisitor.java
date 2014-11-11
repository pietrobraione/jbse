package jbse.tree;

/**
 * Visitor interface for {@link DecisionAlternativeAload}s.
 * 
 * @author Pietro Braione
 */
public interface DecisionAlternativeAloadVisitor {
	void visitDecisionAlternativeAloadOut(DecisionAlternativeAloadOut dao) throws Exception;
	void visitDecisionAlternativeAloadRefExpands(DecisionAlternativeAloadRefExpands dac) throws Exception;
	void visitDecisionAlternativeAloadRefAliases(DecisionAlternativeAloadRefAliases dai) throws Exception;
	void visitDecisionAlternativeAloadRefNull(DecisionAlternativeAloadRefNull dan) throws Exception;
	void visitDecisionAlternativeAloadResolved(DecisionAlternativeAloadResolved dav) throws Exception;
}
