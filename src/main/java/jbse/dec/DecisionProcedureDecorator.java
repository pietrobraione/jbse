package jbse.dec;

import java.util.Collection;
import java.util.Map;

import jbse.bc.ClassHierarchy;
import jbse.dec.exc.DecisionException;
import jbse.dec.exc.InvalidInputException;
import jbse.mem.Clause;
import jbse.mem.Objekt;
import jbse.val.Expression;
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolic;
import jbse.val.ReferenceSymbolic;
import jbse.val.Simplex;

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
	throws InvalidInputException, DecisionException {
		this.component.pushAssumption(c);
	}

	@Override
	public void clearAssumptions() throws DecisionException {
		this.component.clearAssumptions();
	}

	@Override
	public void addAssumptions(Iterable<Clause> assumptionsToAdd) 
	throws InvalidInputException, DecisionException {
		this.component.addAssumptions(assumptionsToAdd);
	}
	
	@Override
	public void setAssumptions(Collection<Clause> newAssumptions) 
	throws InvalidInputException, DecisionException {
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
	public boolean isSat(ClassHierarchy hier, Expression exp) 
	throws InvalidInputException, DecisionException {
		return this.component.isSat(hier, exp);
	}

	@Override
	public boolean isSatNull(ClassHierarchy hier, ReferenceSymbolic r) 
	throws InvalidInputException, DecisionException {
		return this.component.isSatNull(hier, r);
	}

	@Override
	public boolean isSatAliases(ClassHierarchy hier, ReferenceSymbolic r, long heapPos, Objekt o)
	throws InvalidInputException, DecisionException {
		return this.component.isSatAliases(hier, r, heapPos, o);
	}

	@Override
	public boolean isSatExpands(ClassHierarchy hier, ReferenceSymbolic r, String className)
	throws InvalidInputException, DecisionException {
		return this.component.isSatExpands(hier, r, className);
	}

	@Override
	public boolean isSatInitialized(ClassHierarchy hier, String className) 
	throws InvalidInputException, DecisionException {
		return this.component.isSatInitialized(hier, className);
	}

	@Override
	public boolean isSatNotInitialized(ClassHierarchy hier, String className) 
	throws InvalidInputException, DecisionException {
		return this.component.isSatNotInitialized(hier, className);
	}
	
	@Override
	public Map<PrimitiveSymbolic, Simplex> getModel() throws DecisionException {
	    return this.component.getModel();
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
