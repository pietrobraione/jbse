package jbse.apps.run;

import java.util.Map;
import java.util.function.Supplier;

import jbse.bc.ClassFile;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedure;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.dec.DecisionProcedureChainOfResponsibility;
import jbse.dec.exc.DecisionException;
import jbse.jvm.RunnerParameters;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.CannotAssumeSymbolicObjectException;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.meta.annotations.ConservativeRepOk;
import jbse.val.ReferenceSymbolic;
import jbse.val.exc.InvalidTypeException;

/**
 * {@link DecisionProcedureAlgorithms} based on the execution of conservative 
 * repOk methods. It validates a path condition by building the corresponding 
 * initial state and running the methods annotated with the {@link ConservativeRepOk}
 * annotation on all the object of the corresponding classes that are present
 * in the initial state's heap.  
 * 
 * @author Pietro Braione
 */
public final class DecisionProcedureConservativeRepOk extends DecisionProcedureChainOfResponsibility {
    private final InitialHeapChecker checker;

    public DecisionProcedureConservativeRepOk(DecisionProcedure next, 
                                              RunnerParameters checkerParameters, Map<String, String> checkMethods) 
    throws InvalidInputException {
        super(next);
        this.checker = new InitialHeapChecker(checkerParameters, ConservativeRepOk.class, checkMethods);
    }

    @Override
    public void setInitialStateSupplier(Supplier<State> initialStateSupplier) {
        this.checker.setInitialStateSupplier(initialStateSupplier);
    }

    @Override
    public void setCurrentStateSupplier(Supplier<State> currentStateSupplier) {
        this.checker.setCurrentStateSupplier(currentStateSupplier);
    }

    @Override
    protected boolean isSatExpandsLocal(ReferenceSymbolic r, ClassFile classFile)
    throws DecisionException {
        final State sIni = this.checker.makeInitialState();
        try {
            //TODO shall we also assume that classFile and r's static type are initialized? In such case, how do we inject the ExecutionContext?
            sIni.assumeExpands(this.calc, r, classFile);
        } catch (CannotAssumeSymbolicObjectException e) {
            return false;
        } catch (InvalidInputException | InvalidTypeException | 
                 ContradictionException | HeapMemoryExhaustedException e) {
            //this should not happen
            throw new UnexpectedInternalException(e);
        }
        return this.checker.checkHeap(sIni, true);
    }

    @Override
    protected boolean isSatAliasesLocal(ReferenceSymbolic r, long heapPosition, Objekt o) 
    throws DecisionException {
        final State sIni = this.checker.makeInitialState();
        try {
            //TODO shall we also assume that r's static type is initialized? In such case, how do we inject the ExecutionContext?
            sIni.assumeAliases(r, o.getOrigin());
        } catch (ContradictionException | InvalidInputException e) {
            //this should not happen
            throw new UnexpectedInternalException(e);
        }
        return this.checker.checkHeap(sIni, true);
    }

    @Override
    protected boolean isSatNullLocal(ReferenceSymbolic r)
    throws DecisionException {
        final State sIni = this.checker.makeInitialState();
        try {
            sIni.assumeNull(r);
        } catch (ContradictionException | InvalidInputException e) {
            //this should not happen
            throw new UnexpectedInternalException(e);
        }
        return this.checker.checkHeap(sIni, true);
    }
}
