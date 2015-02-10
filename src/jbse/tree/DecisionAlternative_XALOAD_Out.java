package jbse.tree;

import jbse.val.Expression;

/**
 * {@link DecisionAlternative_XALOAD} for the case a read access to an array
 * was out of bounds.
 * 
 * @author Pietro Braione
 */
public final class DecisionAlternative_XALOAD_Out extends DecisionAlternative_XALOAD {
	private static final int OUT_BN = 1;
	
	/**
	 * Constructor, nonconcrete.
	 * 
	 * @param exp The array access {@link Expression}.
	 */
	public DecisionAlternative_XALOAD_Out(Expression exp) {
		super(ALT_CODE + "OUT", exp, OUT_BN);
	}

	/**
	 * Constructor, concrete.
	 */
	public DecisionAlternative_XALOAD_Out() {
		super(ALT_CODE + "OUT", null, OUT_BN);
	}
	
	@Override
	public void accept(VisitorDecisionAlternative_XALOAD v) throws Exception {
		v.visitDecisionAlternative_XALOAD_Out(this);
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
