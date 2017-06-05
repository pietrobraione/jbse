package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.bc.Signatures.INDEX_OUT_OF_BOUNDS_EXCEPTION;
import static jbse.bc.Signatures.JAVA_THROWABLE_BACKTRACE;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Array;
import jbse.mem.Array.AccessOutcomeIn;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Simplex;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

public final class Algo_JAVA_THROWABLE_GETSTACKTRACEELEMENT extends Algo_INVOKEMETA_Nonbranching {
    private Reference thisObject; //set by cookMore
    private Primitive index; //set by cookMore
    private Array backtrace; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }
    
    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException {
        try {
            this.thisObject = (Reference) this.data.operand(0);
            this.index = (Primitive) this.data.operand(1);
            if (!(index instanceof Simplex)) { //quite unlikely...
                throw new SymbolicValueNotAllowedException("the index parameter to java.lang.Throwable.getStackTraceElement method cannot be a symbolic int");
            }
            final int indexInt = (int) ((Simplex) index).getActualValue();
            this.backtrace = (Array) state.getObject((Reference) state.getObject(thisObject).getFieldValue(JAVA_THROWABLE_BACKTRACE));
            final int stackDepth = (int) ((Simplex) this.backtrace.getLength()).getActualValue();
            if (indexInt < 0 || indexInt >= stackDepth) {
                throwNew(state, INDEX_OUT_OF_BOUNDS_EXCEPTION);
                exitFromAlgorithm();
            }
        } catch (ClassCastException e) {
            //this should not happen
            failExecution(e);
        }
    }
    
    @Override
    protected void update(State state) 
    throws InterruptException, SymbolicValueNotAllowedException, ClasspathException, 
    DecisionException {
        try {
            final AccessOutcomeIn outcome = (AccessOutcomeIn) this.backtrace.get(this.index).iterator().next();
            state.pushOperand(outcome.getValue());
        } catch (InvalidOperandException | InvalidTypeException | ThreadStackEmptyException e) {
            //this should not happen
            failExecution(e);
        }
    }
}
