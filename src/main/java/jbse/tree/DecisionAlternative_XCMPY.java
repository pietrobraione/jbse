package jbse.tree;

import jbse.val.Operator;

public abstract class DecisionAlternative_XCMPY implements DecisionAlternative {
    private final int value;
    private final Operator op;
    private final boolean isConcrete;
    private final int branchNumber;
	public static enum Values { GT, EQ, LT };

    protected DecisionAlternative_XCMPY(int value, Operator op, boolean isConcrete, int branchNumber) { 
    	this.value = value; 
    	this.op = op;
    	this.isConcrete = isConcrete;
    	this.branchNumber = branchNumber;
    }

    public final int value() { 
    	return this.value; 
    }
    
    public final Operator operator() { 
    	return this.op; 
    }
    
    public static DecisionAlternative_XCMPY toConcrete(Values v) {
    	switch (v) {
    	case GT:
    		return new DecisionAlternative_XCMPY_Gt(true);
    	case EQ:
    		return new DecisionAlternative_XCMPY_Eq(true);
    	default: //case LT
    		return new DecisionAlternative_XCMPY_Lt(true);
    	}
    }

    public static DecisionAlternative_XCMPY toNonconcrete(Values v) {
    	switch (v) {
    	case GT:
    		return new DecisionAlternative_XCMPY_Gt(false);
    	case EQ:
    		return new DecisionAlternative_XCMPY_Eq(false);
    	default: //case LT
    		return new DecisionAlternative_XCMPY_Lt(false);
    	}
    }

	@Override
	public final String getIdentifier() {
		return toString();
	}
	
	@Override
	public final int getBranchNumber() {
		return this.branchNumber;
	}

	@Override
	public final boolean trivial() {
		return this.isConcrete;
	}
    
    @Override
    public final boolean concrete() {
        return this.isConcrete;
    }
    
    @Override
    public final boolean noDecision() {
        return false;
    }
}
