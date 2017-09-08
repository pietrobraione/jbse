package jbse.apps;

import static jbse.apps.Util.formatClause;
import static jbse.apps.Util.formatClauses;
import static jbse.apps.Util.formatExpression;

import java.io.PrintStream;
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
		this.out = out.clone();
	}

	@Override
	public void pushAssumption(Clause c) 
	throws InvalidInputException, DecisionException {
		super.pushAssumption(c);
		IO.println(this.out, ":: Pushed: " + formatClause(c) + ".");
	}
	
	@Override
	public void clearAssumptions() 
	throws DecisionException {
		super.clearAssumptions();
        IO.println(this.out, ":: Cleared.");
	}
	
	@Override
	public void setAssumptions(Collection<Clause> newAssumptions) 
	throws InvalidInputException, DecisionException {
		super.setAssumptions(newAssumptions);
        IO.print(this.out, ":: Set: ");
        IO.println(this.out, formatClauses(newAssumptions));
    	IO.println(this.out, ".");
	}
	
	@Override
	public boolean isSat(ClassHierarchy hier, Expression exp) 
	throws InvalidInputException, DecisionException {
		final boolean retVal = super.isSat(hier, exp);
        IO.print(this.out, ":: Decided: ");
        IO.print(this.out, formatClauses(this.getAssumptions())); 
        IO.println(this.out, TURNSTILE + formatExpression(exp) + ". Result: " + Boolean.toString(retVal));
        return retVal;
	}
	
	@Override
	public boolean isSatAliases(ClassHierarchy hier, ReferenceSymbolic r, long heapPos, Objekt o)
	throws InvalidInputException, DecisionException {
	    final boolean retVal = super.isSatAliases(hier, r, heapPos, o);
        IO.print(this.out, ":: Decided: ");
        IO.print(this.out, formatClauses(this.getAssumptions())); 
        IO.println(this.out, TURNSTILE + r.getOrigin() + " == " + o.getOrigin() + ". Result: " + Boolean.toString(retVal));
        return retVal;
	}
	
	@Override
	public boolean isSatExpands(ClassHierarchy hier, ReferenceSymbolic r, String className)
	throws InvalidInputException, DecisionException {
	    final boolean retVal = super.isSatExpands(hier, r, className);
        IO.print(this.out, ":: Decided: ");
        IO.print(this.out, formatClauses(this.getAssumptions())); 
        IO.println(this.out, TURNSTILE + r.getOrigin() + " == fresh " + className + ". Result: " + Boolean.toString(retVal));
        return retVal;
	}
	
	@Override
	public boolean isSatNull(ClassHierarchy hier, ReferenceSymbolic r) 
	throws InvalidInputException, DecisionException {
	    final boolean retVal = super.isSatNull(hier, r);
        IO.print(this.out, ":: Decided: ");
        IO.print(this.out, formatClauses(this.getAssumptions())); 
        IO.println(this.out, TURNSTILE + r.getOrigin() + " == null. Result: " + Boolean.toString(retVal));
        return retVal;
	}
	
	@Override
	public boolean isSatInitialized(ClassHierarchy hier, String className) 
	throws InvalidInputException, DecisionException {
	    final boolean retVal = super.isSatInitialized(hier, className);
        IO.print(this.out, ":: Decided: ");
        IO.print(this.out, formatClauses(this.getAssumptions())); 
        IO.println(this.out, TURNSTILE + "pre_init(" + className + "). Result: " + Boolean.toString(retVal));
        return retVal;
	}
	
	@Override
	public boolean isSatNotInitialized(ClassHierarchy hier, String className)
	throws InvalidInputException, DecisionException {
	    final boolean retVal = super.isSatNotInitialized(hier, className);
        IO.print(this.out, ":: Decided: ");
        IO.print(this.out, formatClauses(this.getAssumptions())); 
        IO.println(this.out, TURNSTILE + "!pre_init(" + className + "). Result: " + Boolean.toString(retVal));
        return retVal;
	}
}
