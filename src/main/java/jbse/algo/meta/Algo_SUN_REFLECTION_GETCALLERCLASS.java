package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.bc.Signatures.JAVA_METHOD_INVOKE;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import java.util.List;
import java.util.ListIterator;
import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.CannotInvokeMethodInCurrentContext;
import jbse.bc.ClassFile;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.Frame;
import jbse.mem.State;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;

public final class Algo_SUN_REFLECTION_GETCALLERCLASS extends Algo_INVOKEMETA_Nonbranching {
    private Reference classRef; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 0;
    }

    @Override
    protected void cookMore(State state)
    throws ClasspathException, CannotInvokeMethodInCurrentContext, 
    InterruptException, InvalidInputException {
        final List<Frame> stack = state.getStack();
        final ListIterator<Frame> it = stack.listIterator(stack.size());
        boolean firstSkipped = false;
        ClassFile classFile = null;
        while (it.hasPrevious()) {
            final Frame f = it.previous();
            if (firstSkipped) {
                //skip frames associated with java.lang.reflect.Method.invoke() and its implementation
                //(seemingly just java.lang.reflect.Method.invoke())
                if (f.getMethodSignature().equals(JAVA_METHOD_INVOKE)) {
                    continue;
                }

                classFile = f.getMethodClass();
                break;
            } else {
                firstSkipped = true;
            }
        }
        if (classFile == null) {
            //this should happen only if this method is invoked by the root frame
            throw new CannotInvokeMethodInCurrentContext("The sun.reflect.Reflection.getCallerClass was invoked in a context where there is no caller (arguably the root frame).");
        }

        try {
            state.ensureInstance_JAVA_CLASS(this.ctx.getCalculator(), classFile);
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, this.ctx.getCalculator(), OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        }
        
        this.classRef = state.referenceToInstance_JAVA_CLASS(classFile);
    }


    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.classRef);
        };
    }
}
