package jbse.tree;

import jbse.val.Expression;
import jbse.val.Value;

/**
 * {@link DecisionAlternativeAload} for the case a read access to an array
 * was out of bounds.
 * 
 * @author Pietro Braione
 */
public class DecisionAlternativeAloadOut extends DecisionAlternativeAloadNoResolution {
	private static final int OUT_BN = 1;
	
	/**
	 * Constructor, nonconcrete.
	 * 
	 * @param exp The array access {@link Expression}.
	 */
	public DecisionAlternativeAloadOut(Expression exp) {
		super(ALT_CODE + "OUT", exp, OUT_BN);
	}

	/**
	 * Constructor, concrete.
	 */
	public DecisionAlternativeAloadOut() {
		super(ALT_CODE + "OUT", null, OUT_BN);
	}
	
	@Override
	public boolean ok() {
		return false;
	}

	@Override
	public DecisionAlternativeAloadOut mkNonconcrete(Expression accessExpression, Value valueToLoad, boolean fresh) {
		return new DecisionAlternativeAloadOut(accessExpression); //valueToLoad and fresh unused
	}

	@Override
	public void accept(DecisionAlternativeAloadVisitor v) throws Exception {
		v.visitDecisionAlternativeAloadOut(this);
	}

	@Override
	public boolean concrete() {
		return (this.getArrayAccessExpression() == null);
	}		

	@Override
	public boolean trivial() {
		return this.concrete();
	}
	//TODO equals, hashCode, toString
}
