package jbse.tree;

/**
 * A {@link DecisionAlternative} for the case
 * where no decision must be taken. It is a 
 * Singleton.
 * 
 * @author Pietro Braione
 *
 */
public final class DecisionAlternative_NONE implements DecisionAlternative {
    /**
     * Do not instantiate!
     */
    private DecisionAlternative_NONE() {
        //nothing to do
    }

    private static final DecisionAlternative_NONE INSTANCE = new DecisionAlternative_NONE();
    
    public static DecisionAlternative_NONE instance() {
        return INSTANCE;
    }

    @Override
    public String getIdentifier() {
        return "-";
    }

    @Override
    public int getBranchNumber() {
        return 1;
    }

    @Override
    public boolean trivial() {
        return true;
    }

    @Override
    public boolean concrete() {
        return true;
    }
    
    @Override
    public final boolean noDecision() {
        return true;
    }
}
