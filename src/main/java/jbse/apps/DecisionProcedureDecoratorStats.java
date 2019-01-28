package jbse.apps;

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
    public boolean isSat(Expression exp) 
    throws InvalidInputException, DecisionException {
        this.startTimer();
        final boolean result = super.isSat(exp);
        final long elapsed = this.elapsed();
        System.err.println("ISSAT\t" + exp + "\t" + result + "\t" + elapsed);
        return result;
    }

    @Override
    public boolean isSatAliases(ReferenceSymbolic r, long heapPos, Objekt o)
    throws InvalidInputException, DecisionException {
        this.startTimer();
        final boolean result = super.isSatAliases(r, heapPos, o);
        final long elapsed = this.elapsed();
        System.err.println("ISSATALIASES\t" + r + "\t" + heapPos + "\t" + o + "\t" + result + "\t" + elapsed);
        return result;
    }

    @Override
    public boolean isSatExpands(ReferenceSymbolic r, ClassFile classFile)
    throws InvalidInputException, DecisionException {
        this.startTimer();
        final boolean result = super.isSatExpands(r, classFile);
        final long elapsed = this.elapsed();
        System.err.println("ISSATEXPANDS\t" + r + "\t" + classFile.getClassName() + "\t" + result + "\t" + elapsed);
        return result;
    }

    @Override
    public boolean isSatNull(ReferenceSymbolic r) 
    throws InvalidInputException, DecisionException {
        this.startTimer();
        final boolean result = super.isSatNull(r);
        final long elapsed = this.elapsed();
        System.err.println("ISSATNULL\t" + r + "\t" + result + "\t" + elapsed);
        return result;
    }

    @Override
    public boolean isSatInitialized(ClassFile classFile) 
    throws InvalidInputException, DecisionException {
        this.startTimer();
        final boolean result = super.isSatInitialized(classFile);
        final long elapsed = this.elapsed();
        System.err.println("ISSATINITIALIZED\t" + classFile.getClassName() + "\t" + result + "\t" + elapsed);
        return result;
    }

    @Override
    public boolean isSatNotInitialized(ClassFile classFile)
    throws InvalidInputException, DecisionException {
        this.startTimer();
        final boolean result = super.isSatInitialized(classFile);
        final long elapsed = this.elapsed();
        System.err.println("ISSATNOTINITIALIZED\t" + classFile.getClassName() + "\t" + result + "\t" + elapsed);
        return result;
    }
}
