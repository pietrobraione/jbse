package jbse.dec;

import java.util.ArrayList;

import jbse.bc.ClassHierarchy;
import jbse.mem.ClauseAssumeClassNotInitialized;
import jbse.rewr.CalculatorRewriting;
import jbse.rules.ClassInitRulesRepo;

/**
 * A decision procedure based on the specification of class initializations.
 * 
 * @author Pietro Braione
 */
public final class DecisionProcedureClassInit extends DecisionProcedureChainOfResponsibility {
	private final ClassInitRulesRepo rulesRepo;
	    
    /**
     * Stores all the class names from the 
     * {@link ClauseAssumeClassNotInitialized} 
     * that are pushed. 
     */
    private final ArrayList<String> notInit = new ArrayList<>();
    
	public DecisionProcedureClassInit(DecisionProcedure next, CalculatorRewriting calc, ClassInitRulesRepo rulesRepo) {
		super(next, calc);
		this.rulesRepo = rulesRepo.clone(); //safety copy
	}

	@Override
	protected void clearAssumptionsLocal() {
		this.notInit.clear();
	}

	@Override
	protected void pushAssumptionLocal(ClauseAssumeClassNotInitialized c) {
		this.notInit.add(c.getClassName());
	}
	
    //TODO support pop of assumptions

	@Override
	protected boolean isSatInitializedLocal(ClassHierarchy hier, String c) {
		//we only support mutually exclusive initialized/not-initialized cases
        //TODO drop mutual exclusion of class initialized/not-initialized cases and branch bytecodes during initialization based on assumptions
		return !isSatNotInitializedLocal(hier, c);
	}

	@Override
	protected boolean isSatNotInitializedLocal(ClassHierarchy hier, String c) {
		return (this.rulesRepo.notInitializedClassesContains(c) || this.notInit.contains(c));
	}
}
