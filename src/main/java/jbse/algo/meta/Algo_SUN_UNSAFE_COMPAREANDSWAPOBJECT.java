package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwVerifyError;
import static jbse.mem.Util.areAlias;
import static jbse.mem.Util.areNotAlias;

import jbse.algo.InterruptException;
import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.mem.State;
import jbse.val.Reference;
import jbse.val.Value;

public final class Algo_SUN_UNSAFE_COMPAREANDSWAPOBJECT extends Algo_SUN_UNSAFE_COMPAREANDSWAPX {
	public Algo_SUN_UNSAFE_COMPAREANDSWAPOBJECT() {
		super("Object");
	}

	@Override
	protected boolean checkCompare(State state, Value current, Value toCompare) 
	throws CannotManageStateException, InterruptException {
		try {
			final Reference refCurrent = (Reference) current;
			final Reference refToCompare = (Reference) toCompare;
			if (areNotAlias(state, refCurrent, refToCompare)) {
				return false;
			} else if (!areAlias(state, refCurrent, refToCompare)) {
				throw new SymbolicValueNotAllowedException("The references to be compared during an invocation to sun.misc.Unsafe.CompareAndSwapObject must be concrete or resolved symbolic");
			}
			return true;
		} catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
		}
		return false; //to keep the compiler happy, but it is unreachable
	}
}
