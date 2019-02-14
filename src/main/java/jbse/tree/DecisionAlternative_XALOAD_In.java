package jbse.tree;

import jbse.val.Expression;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Term;

/**
 * {@link DecisionAlternative_XALOAD} for the case a read access to an array
 * was inbounds.
 * 
 * @author Pietro Braione
 */
public abstract class DecisionAlternative_XALOAD_In 
extends DecisionAlternative_XALOAD implements DecisionAlternative_XYLOAD_GETX_Loads {
    private final boolean fresh;
    private final Reference arrayReference;

    protected DecisionAlternative_XALOAD_In(String branchId, Expression arrayAccessExpression, Term indexFormal, Primitive indexActual, Expression arrayAccessExpressionSimplified, boolean fresh, Reference arrayReference, int branchNumber) {
        super(branchId, arrayAccessExpression, indexFormal, indexActual, arrayAccessExpressionSimplified, branchNumber);
        this.fresh = fresh;
        this.arrayReference = arrayReference;
    }

    public final boolean isValueFresh() {
        return this.fresh;
    }

    public final Reference getArrayReference() {
        return this.arrayReference;
    }
}
