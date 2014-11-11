package jbse.tree;

/**
 * {@link DecisionAlternative} for the array creation (*newarray) bytecodes:
 * newarray, anewarray, multianewarray.
 * 
 * @author Pietro Braione
 */
public abstract class DecisionAlternativeNewarray implements DecisionAlternative {
	private final boolean isConcrete;
	private final int branchNumber;

	protected DecisionAlternativeNewarray(boolean isConcrete, int branchNumber) {
    	this.isConcrete = isConcrete;
		this.branchNumber = branchNumber;
	}
	    
    public static DecisionAlternativeNewarray toConcrete(boolean ok) {
        return (ok ? new DecisionAlternativeNewarrayOK(true) : new DecisionAlternativeNewarrayWrong(true));
    }

    public static DecisionAlternativeNewarray toNonconcrete(boolean ok) {
        return (ok ? new DecisionAlternativeNewarrayOK(false) : new DecisionAlternativeNewarrayWrong(false));
    }

    public abstract boolean ok();

    @Deprecated
	public abstract DecisionAlternativeNewarray mkNonconcrete();
	
	@Override
	public final String getIdentifier() {
		return toString();
	}

	@Override
	public final boolean concrete() {
		return this.isConcrete;
	}
	
	@Override
	public final int getBranchNumber() {
		return this.branchNumber;
	}

	@Override
	public final boolean trivial() {
		return this.concrete();
	}
}
