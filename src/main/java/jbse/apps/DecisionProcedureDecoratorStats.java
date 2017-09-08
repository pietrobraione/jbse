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
 * A {@link DecisionProcedureDecorator} that logs the time spent 
 * by each method invocation for its component.
 *  
 * @author Pietro Braione
 */
public class DecisionProcedureDecoratorStats extends DecisionProcedureDecorator {
	private long start;
	
	private void startTimer() {
		this.start = System.currentTimeMillis();
	}

	private long elapsed() {
		final long elapsed = System.currentTimeMillis() - this.start;
		return elapsed;
	}

	public DecisionProcedureDecoratorStats(DecisionProcedure component) {
		super(component);
	}
	
	@Override
	public void pushAssumption(Clause c) 
	throws InvalidInputException, DecisionException {
		this.startTimer();
		super.pushAssumption(c);
		final long elapsed = this.elapsed();
		System.err.println("PUSH\t" + c + "\t\t" + elapsed);
	}
	
	@Override
	public void clearAssumptions() 
	throws DecisionException {
		this.startTimer();
		super.clearAssumptions();
		final long elapsed = this.elapsed();
		System.err.println("CLEAR\t\t\t" + elapsed);
	}
	
	@Override
	public void setAssumptions(Collection<Clause> newAssumptions) 
	throws InvalidInputException, DecisionException {
		this.startTimer();
		super.setAssumptions(newAssumptions);
		final long elapsed = this.elapsed();
		System.err.println("SETASSUMPTIONS\t\t\t" + elapsed);
	}

	@Override
	public Collection<Clause> getAssumptions() 
	throws DecisionException {
		this.startTimer();
		final Collection<Clause> result = super.getAssumptions();
		final long elapsed = this.elapsed();
		System.err.println("GETASSUMPTIONS\t\t" + result + "\t" + elapsed);
		return result;
	}

	@Override
	public boolean isSat(ClassHierarchy hier, Expression exp) 
	throws InvalidInputException, DecisionException {
		this.startTimer();
		final boolean result = super.isSat(hier, exp);
		final long elapsed = this.elapsed();
		System.err.println("ISSAT\t" + exp + "\t" + result + "\t" + elapsed);
        return result;
	}
	
	@Override
	public boolean isSatAliases(ClassHierarchy hier, ReferenceSymbolic r, long heapPos, Objekt o)
	throws InvalidInputException, DecisionException {
		this.startTimer();
		final boolean result = super.isSatAliases(hier, r, heapPos, o);
		final long elapsed = this.elapsed();
		System.err.println("ISSATALIASES\t" + r + "\t" + heapPos + "\t" + o + "\t" + result + "\t" + elapsed);
        return result;
	}
	
	@Override
	public boolean isSatExpands(ClassHierarchy hier, ReferenceSymbolic r, String className)
	throws InvalidInputException, DecisionException {
		this.startTimer();
		final boolean result = super.isSatExpands(hier, r, className);
		final long elapsed = this.elapsed();
		System.err.println("ISSATEXPANDS\t" + r + "\t" + className + "\t" + result + "\t" + elapsed);
        return result;
	}
	
	@Override
	public boolean isSatNull(ClassHierarchy hier, ReferenceSymbolic r) 
	throws InvalidInputException, DecisionException {
		this.startTimer();
		final boolean result = super.isSatNull(hier, r);
		final long elapsed = this.elapsed();
		System.err.println("ISSATNULL\t" + r + "\t" + result + "\t" + elapsed);
        return result;
	}
	
	@Override
	public boolean isSatInitialized(ClassHierarchy hier, String className) 
	throws InvalidInputException, DecisionException {
		this.startTimer();
		final boolean result = super.isSatInitialized(hier, className);
		final long elapsed = this.elapsed();
		System.err.println("ISSATINITIALIZED\t" + className + "\t" + result + "\t" + elapsed);
        return result;
	}
	
	@Override
	public boolean isSatNotInitialized(ClassHierarchy hier, String className)
	throws InvalidInputException, DecisionException {
		this.startTimer();
		final boolean result = super.isSatInitialized(hier, className);
		final long elapsed = this.elapsed();
		System.err.println("ISSATNOTINITIALIZED\t" + className + "\t" + result + "\t" + elapsed);
        return result;
	}
}
