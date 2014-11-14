package jbse.dec;

import java.util.Collection;

import jbse.dec.exc.DecisionException;
import jbse.mem.Clause;
import jbse.mem.Objekt;
import jbse.val.Expression;
import jbse.val.Primitive;
import jbse.val.ReferenceSymbolic;

/**
 * A decision procedure which delegates its operation to another one 
 * without adding any other functionality. It still has a calculator and
 * a set of rewriters for its own sake, but it does not use them
 * (it's up to subclasses to decide when and how to use them). 
 * Used as base for Decorators.
 * 
 * @author Pietro Braione
 *
 */
public class DecisionProcedureDecorator implements DecisionProcedure {
	private final DecisionProcedure component;
	
	public DecisionProcedureDecorator(DecisionProcedure component) {
		this.component = component;
	}
	
	@Override
	public void goFastAndImprecise() {
		this.component.goFastAndImprecise();
	}
	
	@Override
	public void stopFastAndImprecise() {
		this.component.stopFastAndImprecise();
	}
	
	@Override
	public void pushAssumption(Clause c) 
	throws DecisionException {
		this.component.pushAssumption(c);
	}

	@Override
	public void clearAssumptions() 
	throws DecisionException {
		this.component.clearAssumptions();
	}

	@Override
	public void addAssumptions(Iterable<Clause> assumptionsToAdd) 
	throws DecisionException {
		this.component.addAssumptions(assumptionsToAdd);
	}
	
	@Override
	public void setAssumptions(Collection<Clause> newAssumptions) 
	throws DecisionException {
		this.component.setAssumptions(newAssumptions);
	}
	
	//we do not implement setAssumptions(Collection<Clause> newAssumptions)
	//because it is just a different interface to the previous method
	
	@Override
	public Collection<Clause> getAssumptions() 
	throws DecisionException {
		return this.component.getAssumptions();
	}

	@Override
	public boolean isSat(Expression exp) 
	throws DecisionException {
		return this.component.isSat(exp);
	}

	@Override
	public boolean isSatNull(ReferenceSymbolic r) 
	throws DecisionException {
		return this.component.isSatNull(r);
	}

	@Override
	public boolean isSatAliases(ReferenceSymbolic r, long heapPos, Objekt o)
	throws DecisionException {
		return this.component.isSatAliases(r, heapPos, o);
	}

	@Override
	public boolean isSatExpands(ReferenceSymbolic r, String className)
	throws DecisionException {
		return this.component.isSatExpands(r, className);
	}

	@Override
	public boolean isSatInitialized(String className) 
	throws DecisionException {
		return this.component.isSatInitialized(className);
	}

	@Override
	public boolean isSatNotInitialized(String className) 
	throws DecisionException {
		return this.component.isSatNotInitialized(className);
	}

	@Override
	public Primitive simplify(Primitive c) {
		return this.component.simplify(c);
	}
	
	@Override
	public void close() 
	throws DecisionException {
		this.component.close();
	}
}
