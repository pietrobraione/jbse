package jbse.apps;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import jbse.bc.ClassFile;
import jbse.common.exc.InvalidInputException;
import jbse.dec.DecisionProcedure;
import jbse.dec.DecisionProcedureDecorator;
import jbse.dec.exc.DecisionException;
import jbse.mem.Clause;
import jbse.mem.Objekt;
import jbse.val.Expression;
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolic;
import jbse.val.ReferenceSymbolic;
import jbse.val.Simplex;

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
        this.time += System.currentTimeMillis() - this.start;
    }

    public DecisionProcedureDecoratorTimer(DecisionProcedure component) throws InvalidInputException {
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
        startTimer();
        super.pushAssumption(c);
        stopTimer();
    }

    @Override
    public void clearAssumptions() 
    throws DecisionException {
        startTimer();
        super.clearAssumptions();
        stopTimer();
    }
    
    @Override
    public void addAssumptions(Iterable<Clause> assumptionsToAdd) 
    throws InvalidInputException, DecisionException {
        startTimer();
        super.addAssumptions(assumptionsToAdd);
        stopTimer();
    }
    
    @Override
    public void addAssumptions(Clause... assumptionsToAdd) 
    throws InvalidInputException, DecisionException {
        startTimer();
        super.addAssumptions(assumptionsToAdd);
        stopTimer();
    }

    @Override
    public void setAssumptions(Collection<Clause> newAssumptions) 
    throws InvalidInputException, DecisionException {
        startTimer();
        super.setAssumptions(newAssumptions);
        stopTimer();
    }

    @Override
    public List<Clause> getAssumptions() 
    throws DecisionException {
        startTimer();
        final List<Clause> result = super.getAssumptions();
        stopTimer();
        return result;
    }

    @Override
    public boolean isSat(Expression exp) 
    throws InvalidInputException, DecisionException {
        startTimer();
        final boolean result = super.isSat(exp);
        stopTimer();
        return result;
    }

    @Override
    public boolean isSatNull(ReferenceSymbolic r) 
    throws InvalidInputException, DecisionException {
        startTimer();
        final boolean result = super.isSatNull(r);
        stopTimer();
        return result;
    }

    @Override
    public boolean isSatAliases(ReferenceSymbolic r, long heapPos, Objekt o)
    throws InvalidInputException, DecisionException {
        startTimer();
        final boolean result = super.isSatAliases(r, heapPos, o);
        stopTimer();
        return result;
    }

    @Override
    public boolean isSatExpands(ReferenceSymbolic r, ClassFile classFile)
    throws InvalidInputException, DecisionException {
        startTimer();
        final boolean result = super.isSatExpands(r, classFile);
        stopTimer();
        return result;
    }

    @Override
    public boolean isSatInitialized(ClassFile classFile) 
    throws InvalidInputException, DecisionException {
        startTimer();
        final boolean result = super.isSatInitialized(classFile);
        stopTimer();
        return result;
    }

    @Override
    public boolean isSatNotInitialized(ClassFile classFile)
    throws InvalidInputException, DecisionException {
        startTimer();
        final boolean result = super.isSatNotInitialized(classFile);
        stopTimer();
        return result;
    }
    
    @Override
    public Map<PrimitiveSymbolic, Simplex> getModel() throws DecisionException {
        startTimer();
        final Map<PrimitiveSymbolic, Simplex> result = super.getModel();
        stopTimer();
        return result;
    }
    
    @Override
    public Primitive simplify(Primitive c) throws DecisionException {
        startTimer();
        final Primitive result = super.simplify(c);
        stopTimer();
        return result;
    }
}
