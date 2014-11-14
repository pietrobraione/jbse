package jbse.tree;

import jbse.val.Operator;

public abstract class DecisionAlternativeComparison implements DecisionAlternative {
    private final int value;
    private final Operator op;
    private final boolean isConcrete;
    private final int branchNumber;
	public static enum Values { GT, EQ, LT };

    protected DecisionAlternativeComparison(int value, Operator op, boolean isConcrete, int branchNumber) { 
    	this.value = value; 
    	this.op = op;
    	this.isConcrete = isConcrete;
    	this.branchNumber = branchNumber;
    }

    @Deprecated
	public abstract DecisionAlternativeComparison mkNonconcrete();
    
    public final int value() { 
    	return this.value; 
    }
    
    public final Operator toOperator() { 
    	return this.op; 
    }
    
    public static DecisionAlternativeComparison toConcrete(Values v) {
    	switch (v) {
    	case GT:
    		return new DecisionAlternativeComparisonGt(true);
    	case EQ:
    		return new DecisionAlternativeComparisonEq(true);
    	default: //case LT
    		return new DecisionAlternativeComparisonLt(true);
    	}
    }

    public static DecisionAlternativeComparison toNonconcrete(Values v) {
    	switch (v) {
    	case GT:
    		return new DecisionAlternativeComparisonGt(false);
    	case EQ:
    		return new DecisionAlternativeComparisonEq(false);
    	default: //case LT
    		return new DecisionAlternativeComparisonLt(false);
    	}
    }

	@Override
	public final String getIdentifier() {
		return toString();
	}
	
	@Override
	public boolean concrete() {
		return this.isConcrete;
	}
	
	@Override
	public final int getBranchNumber() {
		return this.branchNumber;
	}

	@Override
	public boolean trivial() {
		return this.concrete();
	}
}
