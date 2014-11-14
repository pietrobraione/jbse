package jbse.tree;

import jbse.mem.Util;
import jbse.val.Expression;
import jbse.val.ReferenceSymbolic;
import jbse.val.Value;

/**
 * {@link DecisionAlternativeAload} for the case a read access to an array
 * returned a resolved {@link ReferenceSymbolic} or a numeric value,  
 * either concrete or symbolic.
 * 
 * @author Pietro Braione
 */
public class DecisionAlternativeAloadResolved extends DecisionAlternativeAloadNoResolution implements DecisionAlternativeLoadResolved {
	private static final int RESOLVED_BN = 1;
	
	private final Value valueToLoad;
	private final boolean fresh;
	private final boolean isConcrete;
	private final boolean isTrivial;

	/**
	 * Constructor, nonconcrete.
	 * 
	 * @param arrayAccessExpression The array access {@link Expression}.
	 * @param valueToLoad The {@link Value} loaded from the array.
	 * @param fresh {@code true} iff {@code valToLoad} is fresh, i.e., 
	 *        is not stored in the array and, therefore, must be written
	 *        back to the array.
	 */
	public DecisionAlternativeAloadResolved(Expression arrayAccessExpression, Value valueToLoad, boolean fresh) {
		super(ALT_CODE + "R", arrayAccessExpression, RESOLVED_BN);
		this.valueToLoad = valueToLoad;
		this.fresh = fresh;
		this.isConcrete = (arrayAccessExpression == null) && !Util.isSymbolicReference(valueToLoad);
		this.isTrivial = (arrayAccessExpression == null);
	}

	/**
	 * Constructor, concrete.
	 * 
	 * @param valueToLoad The {@link Value} loaded from the array.
	 * @param fresh {@code true} iff {@code valToLoad} is fresh, i.e., 
	 *        is not stored in the array and, therefore, must be written
	 *        back to the array.
	 */
	public DecisionAlternativeAloadResolved(Value valueToLoad, boolean fresh) {
		this(null, valueToLoad, fresh);
	}
	
	public boolean isValueFresh() {
		return this.fresh;
	}
	
	@Override
	public boolean ok() {
		return true;
	}
	
	@Override
	public DecisionAlternativeAloadResolved mkNonconcrete(Expression exp, Value valueToLoad, boolean fresh) {
		return new DecisionAlternativeAloadResolved(exp, valueToLoad, fresh);
	}

	@Override
	public void accept(DecisionAlternativeAloadVisitor v) throws Exception {
		v.visitDecisionAlternativeAloadResolved(this);
	}

	@Override
	public boolean concrete() {
		return this.isConcrete;
	}

	@Override
	public boolean trivial() {
		return this.isTrivial;
	}

	@Override
	public Value getValueToLoad() {
		return this.valueToLoad;
	}
	//TODO equals, hashCode, toString
}
