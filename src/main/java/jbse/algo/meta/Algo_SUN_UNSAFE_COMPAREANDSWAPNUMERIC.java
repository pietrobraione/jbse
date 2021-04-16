package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwVerifyError;

import jbse.algo.InterruptException;
import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.common.exc.ClasspathException;
import jbse.mem.State;
import jbse.val.Primitive;
import jbse.val.Simplex;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#compareAndSwapInt(Object, long, int, int)}
 * and {@link sun.misc.Unsafe#compareAndSwapLong(Object, long, long, long)}.
 * 
 * @author Pietro Braione
 */
public abstract class Algo_SUN_UNSAFE_COMPAREANDSWAPNUMERIC extends Algo_SUN_UNSAFE_COMPAREANDSWAPX {
    public Algo_SUN_UNSAFE_COMPAREANDSWAPNUMERIC(String what) {
        super(what);
    }

    @Override
    protected final boolean checkCompare(State state, Value current, Value toCompare) 
    throws CannotManageStateException, InterruptException, ClasspathException {
        try {
            final Primitive primCurrent = (Primitive) current;
            final Primitive primToCompare = (Primitive) toCompare;
            if (primCurrent instanceof Simplex && primToCompare instanceof Simplex) {
                //TODO check they are of the right type, or at least that they are of the same type?
                return ((Simplex) this.ctx.getCalculator().push(primCurrent).eq(primToCompare).pop()).surelyTrue();
            } else {
                throw new SymbolicValueNotAllowedException("The numbers to be compared during an invocation to sun.misc.Unsafe.CompareAndSwap" + this.what + " must be concrete (branching currently not implemented)");
            }
        } catch (ClassCastException | InvalidTypeException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        } catch (InvalidOperandException e) {
            //this should never happen
            failExecution(e);
        }
        return false; //to keep the compiler happy, but it is unreachable
    }
}
