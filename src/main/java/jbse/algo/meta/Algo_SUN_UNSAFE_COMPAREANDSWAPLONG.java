package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwVerifyError;

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
 * Meta-level implementation of {@link sun.misc.Unsafe#compareAndSwapLong(Object, long, long, long)}.
 * 
 * @author Pietro Braione
 */
//TODO merge with Algo_SUN_UNSAFE_COMPAREANDSWAPINT
public final class Algo_SUN_UNSAFE_COMPAREANDSWAPLONG extends Algo_SUN_UNSAFE_COMPAREANDSWAPX {
    public Algo_SUN_UNSAFE_COMPAREANDSWAPLONG() {
        super("Long");
    }

    @Override
    protected boolean checkCompare(State state, Value current, Value toCompare) 
    throws CannotManageStateException, InterruptException, ClasspathException {
        try {
            final Primitive primCurrent = (Primitive) current;
            final Primitive primToCompare = (Primitive) toCompare;
            if (primCurrent instanceof Simplex && primToCompare instanceof Simplex) {
                //TODO check they are longs, or at least that they are of the same type?
                return ((Simplex) this.ctx.getCalculator().push(primCurrent).eq(primToCompare).pop()).surelyTrue();
            } else {
                throw new SymbolicValueNotAllowedException("The longs to be compared during an invocation to sun.misc.Unsafe.CompareAndSwapLong must be concrete (branching currently not implemented)");
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
