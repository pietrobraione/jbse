package jbse.apps;

import static jbse.apps.Util.formatClause;
import static jbse.apps.Util.formatClauses;
import static jbse.apps.Util.formatExpression;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;

import jbse.bc.ClassFile;
import jbse.common.exc.InvalidInputException;
import jbse.dec.DecisionProcedure;
import jbse.dec.DecisionProcedureDecorator;
import jbse.dec.exc.DecisionException;
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
public final class DecisionProcedureDecoratorPrint extends DecisionProcedureDecorator {
    private static final String TURNSTILE = " |-SAT- ";
    private PrintStream[] out;

    /**
     * Constructor.
     * 
     * @param component the {@link DecisionProcedure} which must be decorated.
     * @param out a varargs of {@link PrintStream}s; The incoming requests 
     *        will be prettyprinted on all the elements of {@code out} after 
     *        being successfully served by {@code component}. 
     * @throws InvalidInputException if {@code component == null}.
     * @throws NullPointerException if {@code out == null}.
     */
    public DecisionProcedureDecoratorPrint(DecisionProcedure component, PrintStream... out) throws InvalidInputException {
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
    public void addAssumptions(Iterable<Clause> assumptionsToAdd) 
    throws InvalidInputException, DecisionException {
        super.addAssumptions(assumptionsToAdd);
        IO.print(this.out, ":: Add: ");
        IO.println(this.out, formatClauses(assumptionsToAdd));
        IO.println(this.out, ".");
    }
    
    @Override
    public void addAssumptions(Clause... assumptionsToAdd) 
    throws InvalidInputException, DecisionException {
        super.addAssumptions(assumptionsToAdd);
        IO.print(this.out, ":: Add: ");
        IO.println(this.out, formatClauses(Arrays.asList(assumptionsToAdd)));
        IO.println(this.out, ".");
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
    public boolean isSat(Expression exp) 
    throws InvalidInputException, DecisionException {
        final boolean retVal = super.isSat(exp);
        IO.print(this.out, ":: Decided: ");
        IO.print(this.out, formatClauses(getAssumptions())); 
        IO.println(this.out, TURNSTILE + formatExpression(exp) + ". Result: " + Boolean.toString(retVal));
        return retVal;
    }

    @Override
    public boolean isSatNull(ReferenceSymbolic r) 
    throws InvalidInputException, DecisionException {
        final boolean retVal = super.isSatNull(r);
        IO.print(this.out, ":: Decided: ");
        IO.print(this.out, formatClauses(getAssumptions())); 
        IO.println(this.out, TURNSTILE + r.asOriginString() + " == null. Result: " + Boolean.toString(retVal));
        return retVal;
    }

    @Override
    public boolean isSatAliases(ReferenceSymbolic r, long heapPos, Objekt o)
    throws InvalidInputException, DecisionException {
        final boolean retVal = super.isSatAliases(r, heapPos, o);
        IO.print(this.out, ":: Decided: ");
        IO.print(this.out, formatClauses(getAssumptions())); 
        IO.println(this.out, TURNSTILE + r.asOriginString() + " == " + o.getOrigin().asOriginString() + ". Result: " + Boolean.toString(retVal));
        return retVal;
    }

    @Override
    public boolean isSatExpands(ReferenceSymbolic r, ClassFile classFile)
    throws InvalidInputException, DecisionException {
        final boolean retVal = super.isSatExpands(r, classFile);
        IO.print(this.out, ":: Decided: ");
        IO.print(this.out, formatClauses(getAssumptions())); 
        IO.println(this.out, TURNSTILE + r.asOriginString() + " == fresh " + classFile.getClassName() + ". Result: " + Boolean.toString(retVal));
        return retVal;
    }

    @Override
    public boolean isSatInitialized(ClassFile classFile) 
    throws InvalidInputException, DecisionException {
        final boolean retVal = super.isSatInitialized(classFile);
        IO.print(this.out, ":: Decided: ");
        IO.print(this.out, formatClauses(getAssumptions())); 
        IO.println(this.out, TURNSTILE + "pre_init(" + classFile.getClassName() + "). Result: " + Boolean.toString(retVal));
        return retVal;
    }

    @Override
    public boolean isSatNotInitialized(ClassFile classFile)
    throws InvalidInputException, DecisionException {
        final boolean retVal = super.isSatNotInitialized(classFile);
        IO.print(this.out, ":: Decided: ");
        IO.print(this.out, formatClauses(getAssumptions())); 
        IO.println(this.out, TURNSTILE + "!pre_init(" + classFile.getClassName() + "). Result: " + Boolean.toString(retVal));
        return retVal;
    }
}
