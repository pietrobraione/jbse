package jbse.apps;

import java.util.Collection;

import jbse.bc.ClassHierarchy;
import jbse.dec.DecisionProcedure;
import jbse.dec.DecisionProcedureDecorator;
import jbse.dec.exc.DecisionException;
import jbse.dec.exc.InvalidInputException;
import jbse.mem.Clause;
import jbse.mem.Objekt;
import jbse.val.Expression;
import jbse.val.ReferenceSymbolic;

/**
 * A {@link DecisionProcedureDecorator} that accounts the time spent 
 * by its component.
 *  
 * @author Pietro Braione
 */
public class DecisionProcedureDecoratorTimer extends DecisionProcedureDecorator implements Timer {
	private long time;
	private long start;
	
	private void startTimer() {
		this.start = System.currentTimeMillis();
	}

	private void stopTimer() {
        this.time += System.currentTimeMillis() - start;
	}

	public DecisionProcedureDecoratorTimer(DecisionProcedure component) {
		super(component);
    	this.time = 0L;
	}

	@Override
	public long getTime() {
		return this.time;
	}
	
	@Override
	public void pushAssumption(Clause c) 
	throws InvalidInputException, DecisionException {
		this.startTimer();
		super.pushAssumption(c);
		this.stopTimer();
	}
	
	@Override
	public void clearAssumptions() 
	throws DecisionException {
		this.startTimer();
		super.clearAssumptions();
		this.stopTimer();
	}
	
	@Override
	public void setAssumptions(Collection<Clause> newAssumptions) 
	throws InvalidInputException, DecisionException {
		this.startTimer();
		super.setAssumptions(newAssumptions);
		this.stopTimer();
	}
	
	@Override
	public Collection<Clause> getAssumptions() 
	throws DecisionException {
		this.startTimer();
		final Collection<Clause> result = super.getAssumptions();
		this.stopTimer();
		return result;
	}

	@Override
	public boolean isSat(ClassHierarchy hier, Expression exp) 
	throws InvalidInputException, DecisionException {
		this.startTimer();
		final boolean result = super.isSat(hier, exp);
		this.stopTimer();
        return result;
	}
	
	@Override
	public boolean isSatAliases(ClassHierarchy hier, ReferenceSymbolic r, long heapPos, Objekt o)
	throws InvalidInputException, DecisionException {
		this.startTimer();
		final boolean result = super.isSatAliases(hier, r, heapPos, o);
		this.stopTimer();
        return result;
	}
	
	@Override
	public boolean isSatExpands(ClassHierarchy hier, ReferenceSymbolic r, String className)
	throws InvalidInputException, DecisionException {
		this.startTimer();
		final boolean result = super.isSatExpands(hier, r, className);
		this.stopTimer();
        return result;
	}
	
	@Override
	public boolean isSatNull(ClassHierarchy hier, ReferenceSymbolic r) 
	throws InvalidInputException, DecisionException {
		this.startTimer();
		final boolean result = super.isSatNull(hier, r);
		this.stopTimer();
        return result;
	}
	
	@Override
	public boolean isSatInitialized(ClassHierarchy hier, String className) 
	throws InvalidInputException, DecisionException {
		this.startTimer();
		final boolean result = super.isSatInitialized(hier, className);
		this.stopTimer();
        return result;
	}
	
	@Override
	public boolean isSatNotInitialized(ClassHierarchy hier, String className)
	throws InvalidInputException, DecisionException {
		this.startTimer();
		final boolean result = super.isSatNotInitialized(hier, className);
		this.stopTimer();
        return result;
	}
}
