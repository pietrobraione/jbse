package jbse.tree;

import jbse.mem.Util;
import jbse.val.Expression;
import jbse.val.Value;

/**
 * {@link DecisionAlternative_XALOAD} for the case where 
 * the value loaded on the operand stack is a previously resolved 
 * symbolic reference or a primitive (either symbolic or concrete) 
 * value.
 * 
 * @author Pietro Braione
 */
public final class DecisionAlternative_XALOAD_Resolved 
extends DecisionAlternative_XALOAD_In implements DecisionAlternative_XYLOAD_GETX_Resolved {
	private final Value valueToLoad;
	private final boolean fresh;
    private final boolean isTrivial;
	private final boolean isConcrete;
	private final int hashCode;

	/**
	 * Constructor, nonconcrete.
	 * 
	 * @param arrayAccessExpression the array access {@link Expression}.
	 * @param valueToLoad the {@link Value} loaded from the array.
	 * @param fresh {@code true} iff {@code valToLoad} is fresh, i.e., 
	 *        is not stored in the array and, therefore, must be written
	 *        back to the array.
	 * @param branchNumber an {@code int}, the branch number.
	 */
	public DecisionAlternative_XALOAD_Resolved(Expression arrayAccessExpression, Value valueToLoad, boolean fresh, int branchNumber) {
		super(ALT_CODE + "_Resolved:" + arrayAccessExpression, arrayAccessExpression, branchNumber);
		this.valueToLoad = valueToLoad;
		this.fresh = fresh;
        this.isTrivial = (arrayAccessExpression == null);
		this.isConcrete = this.isTrivial && !Util.isSymbolicReference(valueToLoad);
        final int prime = 131;
        int result = super.hashCode();
        result = prime * result +
            ((this.valueToLoad == null) ? 0 : this.valueToLoad.hashCode());
        this.hashCode = result;
	}

	/**
	 * Constructor, concrete.
	 * 
	 * @param valueToLoad The {@link Value} loaded from the array.
	 * @param fresh {@code true} iff {@code valToLoad} is fresh, i.e., 
	 *        is not stored in the array and, therefore, must be written
	 *        back to the array.
     * @param branchNumber an {@code int}, the branch number.
	 */
	public DecisionAlternative_XALOAD_Resolved(Value valueToLoad, boolean fresh, int branchNumber) {
		this(null, valueToLoad, fresh, branchNumber);
	}
	
	public boolean isValueFresh() {
		return this.fresh;
	}
	
	@Override
	public void accept(VisitorDecisionAlternative_XALOAD v) throws Exception {
		v.visitDecisionAlternative_XALOAD_Resolved(this);
	}

    @Override
    public boolean trivial() {
        return this.isTrivial;
    }

	@Override
	public boolean concrete() {
		return this.isConcrete;
	}

	@Override
	public Value getValueToLoad() {
		return this.valueToLoad;
	}

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        final DecisionAlternative_XALOAD_Resolved other =
            (DecisionAlternative_XALOAD_Resolved) obj;
        if (this.valueToLoad == null) {
            if (other.valueToLoad != null) {
                return false;
            }
        } else if (!this.valueToLoad.equals(other.valueToLoad)) {
            return false;
        }
        return true;
    }
}
