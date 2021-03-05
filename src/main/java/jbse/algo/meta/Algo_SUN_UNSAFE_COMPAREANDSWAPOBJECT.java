package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.mem.Util.areAlias;
import static jbse.mem.Util.areNotAlias;

import jbse.algo.InterruptException;
import jbse.algo.exc.CannotManageStateException;
import jbse.common.exc.ClasspathException;
import jbse.mem.State;
import jbse.val.Reference;
import jbse.val.Value;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#compareAndSwapObject(Object, long, Object, Object)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_SUN_UNSAFE_COMPAREANDSWAPOBJECT extends Algo_SUN_UNSAFE_COMPAREANDSWAPX {
    public Algo_SUN_UNSAFE_COMPAREANDSWAPOBJECT() {
        super("Object");
    }

    @Override
    protected boolean checkCompare(State state, Value current, Value toCompare) 
    throws CannotManageStateException, InterruptException, ClasspathException {
        try {
            final Reference refCurrent = (Reference) current;
            final Reference refToCompare = (Reference) toCompare;
            if (areNotAlias(state, refCurrent, refToCompare)) {
                return false;
            } else if (!areAlias(state, refCurrent, refToCompare)) {
                //this should never happen
                failExecution("Unexpected unresolved symbolic references as parameters of sun.misc.Unsafe.compareAndSwapObject invocation");
            }
            return true;
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        }
        return false; //to keep the compiler happy, but it is unreachable
    }
}
