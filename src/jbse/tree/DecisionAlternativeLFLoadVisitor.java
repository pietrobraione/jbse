package jbse.tree;

/**
 * Visitor interface for {@link DecisionAlternativeLFLoad}s.
 * 
 * @author Pietro Braione
 */
public interface DecisionAlternativeLFLoadVisitor {
	void visitDecisionAlternativeLFLoadRefExpands(DecisionAlternativeLFLoadRefExpands drc) throws Exception;
	void visitDecisionAlternativeLFLoadRefNull(DecisionAlternativeLFLoadRefNull drn) throws Exception;
	void visitDecisionAlternativeLFLoadRefAliases(DecisionAlternativeLFLoadRefAliases dro) throws Exception;
	void visitDecisionAlternativeLFLoadResolved(DecisionAlternativeLFLoadResolved drr) throws Exception;
}
