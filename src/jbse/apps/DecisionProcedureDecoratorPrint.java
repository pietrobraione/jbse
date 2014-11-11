package jbse.apps;

import static jbse.apps.Util.formatClause;
import static jbse.apps.Util.formatClauses;
import static jbse.apps.Util.formatExpression;

import java.io.PrintStream;
import java.util.Collection;

import jbse.dec.DecisionProcedure;
import jbse.dec.DecisionProcedureDecorator;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.dec.DecisionException;
import jbse.mem.Clause;
import jbse.mem.Expression;
import jbse.mem.Objekt;
import jbse.mem.ReferenceSymbolic;

/**
 * A {@link DecisionProcedureDecorator} which prettyprints on a number of {@link PrintStream}s 
 * the requests to the decorated {@link DecisionProcedure}.
 *  
 * @author Pietro Braione
 */
public class DecisionProcedureDecoratorPrint extends DecisionProcedureDecorator {
	private static final String TURNSTILE = " |-SAT- ";
	private PrintStream[] out;
	
	/**
	 * Constructor.
	 * 
	 * @param component the {@link DecisionProcedure} which must be decorated.
	 * @param out A {@link PrintStream}{@code []}; the incoming requests 
	 *        will be prettyprinted on all the elements of {@code out} after 
	 *        being successfully served by {@code component}. 
	 */
	public DecisionProcedureDecoratorPrint(DecisionProcedure component, PrintStream[] out) {
		super(component);
		this.out = out;
	}

	@Override
	public void pushAssumption(Clause c) throws DecisionException, UnexpectedInternalException {
		super.pushAssumption(c);
		IO.println(this.out, ":: Pushed: " + formatClause(c) + ".");
	}
	
	@Override
	public void clearAssumptions() 
	throws DecisionException, UnexpectedInternalException {
		super.clearAssumptions();
        IO.println(this.out, ":: Cleared.");
	}
	
	@Override
	public void setAssumptions(Collection<Clause> newAssumptions) 
	throws DecisionException, UnexpectedInternalException {
		super.setAssumptions(newAssumptions);
        IO.print(this.out, ":: Set: ");
        IO.println(this.out, formatClauses(newAssumptions));
    	IO.println(this.out, ".");
	}
	
	@Override
	public boolean isSat(Expression exp) 
	throws DecisionException, UnexpectedInternalException {
		boolean retVal = super.isSat(exp);
        IO.print(this.out, ":: Decided: ");
        IO.print(this.out, formatClauses(this.getAssumptions())); 
        IO.println(this.out, TURNSTILE + formatExpression(exp) + ". Result: " + Boolean.toString(retVal));
        return retVal;
	}
	
	@Override
	public boolean isSatAliases(ReferenceSymbolic r, long heapPos, Objekt o)
	throws DecisionException, UnexpectedInternalException {
		boolean retVal = super.isSatAliases(r, heapPos, o);
        IO.print(this.out, ":: Decided: ");
        IO.print(this.out, formatClauses(this.getAssumptions())); 
        IO.println(this.out, TURNSTILE + r.getOrigin() + " == " + o.getOrigin() + ". Result: " + Boolean.toString(retVal));
        return retVal;
	}
	
	@Override
	public boolean isSatExpands(ReferenceSymbolic r, String className)
	throws DecisionException, UnexpectedInternalException {
		boolean retVal = super.isSatExpands(r, className);
        IO.print(this.out, ":: Decided: ");
        IO.print(this.out, formatClauses(this.getAssumptions())); 
        IO.println(this.out, TURNSTILE + r.getOrigin() + " == fresh " + className + ". Result: " + Boolean.toString(retVal));
        return retVal;
	}
	
	@Override
	public boolean isSatNull(ReferenceSymbolic r) 
	throws DecisionException, UnexpectedInternalException {
		boolean retVal = super.isSatNull(r);
        IO.print(this.out, ":: Decided: ");
        IO.print(this.out, formatClauses(this.getAssumptions())); 
        IO.println(this.out, TURNSTILE + r.getOrigin() + " == null. Result: " + Boolean.toString(retVal));
        return retVal;
	}
	
	@Override
	public boolean isSatInitialized(String className) 
	throws DecisionException, UnexpectedInternalException {
		boolean retVal = super.isSatInitialized(className);
        IO.print(this.out, ":: Decided: ");
        IO.print(this.out, formatClauses(this.getAssumptions())); 
        IO.println(this.out, TURNSTILE + "pre_init(" + className + "). Result: " + Boolean.toString(retVal));
        return retVal;
	}
	
	@Override
	public boolean isSatNotInitialized(String className)
	throws DecisionException, UnexpectedInternalException {
		boolean retVal = super.isSatNotInitialized(className);
        IO.print(this.out, ":: Decided: ");
        IO.print(this.out, formatClauses(this.getAssumptions())); 
        IO.println(this.out, TURNSTILE + "!pre_init(" + className + "). Result: " + Boolean.toString(retVal));
        return retVal;
	}
}
