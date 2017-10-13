package jbse.algo.meta;

import static jbse.algo.Util.ensureInstance_JAVA_CLASS;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.JAVA_METHOD_INVOKE;

import java.util.List;
import java.util.ListIterator;
import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.exc.CannotManageStateException;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Frame;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;

public final class Algo_SUN_REFLECTION_GETCALLERCLASS extends Algo_INVOKEMETA_Nonbranching {
    private String className; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 0;
    }

    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException {
        final List<Frame> stack = state.getStack();
        final ListIterator<Frame> it = stack.listIterator(stack.size());
        boolean firstSkipped = false;
        while (it.hasPrevious()) {
            final Frame f = it.previous();
            if (firstSkipped) {
                //skip frames associated with java.lang.reflect.Method.invoke() and its implementation
                //(seemingly just java.lang.reflect.Method.invoke())
                if (f.getCurrentMethodSignature().equals(JAVA_METHOD_INVOKE)) {
                    continue;
                }

                this.className = f.getCurrentMethodSignature().getClassName();
                break;
            } else {
                firstSkipped = true;
            }
        }

        try {
            ensureInstance_JAVA_CLASS(state, this.className, this.className, this.ctx);
        } catch (ClassFileNotAccessibleException e) {
            throwNew(state, ILLEGAL_ACCESS_ERROR);
            exitFromAlgorithm();
        } catch (BadClassFileException e) {
            //this should never happen
            failExecution(e);
        }
    }


    @Override
    protected void update(State state) throws ThreadStackEmptyException {
        //gets the instance of the class of the caller object
        final Reference classRef = state.referenceToInstance_JAVA_CLASS(this.className);
        state.pushOperand(classRef);
    }
}
