package jbse.tree;

import jbse.mem.Util;
import jbse.val.Expression;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Term;
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
    private final boolean isTrivial;
    private final boolean isConcrete;
    private final int hashCode;

    /**
     * Constructor, nonconcrete.
     * 
     * @param arrayAccessExpression an {@link Expression} containing {@code indexFormal}, 
     *        signifying the condition under which the array access yields {@code valToLoad} 
     *        as result. It can be {@code null}, in which case it is equivalent to {@code true} but 
     *        additionally denotes the fact that the array was accessed by a concrete index.
     * @param indexFormal the {@link Term} used in {@code accessExpression} to indicate
     *        the array index. It must not be {@code null}.
     * @param indexActual a {@link Primitive}, the actual index used to access the array.
     *        It must not be {@code null}.
     * @param arrayAccessExpressionSimplified a simplification of {@code arrayAccessExpression}, 
     *        or {@code null} if {@code arrayAccessExpression} simplifies to {@code true}.
     * @param valueToLoad the {@link Value} loaded from the array.
     * @param fresh {@code true} iff {@code valToLoad} is fresh, i.e., 
     *        is not stored in the array and, therefore, must be written
     *        back to the array.
     * @param arrayReference when {@code fresh == true} is a {@link Reference} to the array 
     *        where {@code valueToLoad} originates from.
     * @param branchNumber an {@code int}, the branch number.
     */
    public DecisionAlternative_XALOAD_Resolved(Expression arrayAccessExpression, Term indexFormal, Primitive indexActual, Expression arrayAccessExpressionSimplified, Value valueToLoad, boolean fresh, Reference arrayReference, int branchNumber) {
        super(ALT_CODE + "_Resolved:" + (arrayAccessExpressionSimplified == null ? "true" : arrayAccessExpressionSimplified), arrayAccessExpression, indexFormal, indexActual, arrayAccessExpressionSimplified, fresh, arrayReference, branchNumber);
        this.valueToLoad = valueToLoad;
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
     * @param arrayReference when {@code fresh == true} is a {@link Reference} to the array 
     *        where {@code valueToLoad} originates from.
     * @param branchNumber an {@code int}, the branch number.
     */
    public DecisionAlternative_XALOAD_Resolved(Value valueToLoad, boolean fresh, Reference arrayReference, int branchNumber) {
        this(null, null, null, null, valueToLoad, fresh, arrayReference, branchNumber);
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
        final DecisionAlternative_XALOAD_Resolved other = (DecisionAlternative_XALOAD_Resolved) obj;
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
