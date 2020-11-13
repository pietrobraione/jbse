package jbse.algo.meta;

import static jbse.algo.Util.continueWith;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Array;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.KlassPseudoReference;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Simplex;
import jbse.val.Value;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#putLong(Object, long, long)} and 
 * {@link sun.misc.Unsafe#putLongVolatile(Object, long, long)}.
 */
public final class Algo_SUN_UNSAFE_PUTLONG_O extends Algo_INVOKEMETA_Nonbranching {
	private final Algo_SUN_UNSAFE_PUTLONG_O_Array algoArray = new Algo_SUN_UNSAFE_PUTLONG_O_Array();
	private Objekt obj;
	private int ofst;
	
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 4;
    }
    
    @Override
    protected void cookMore(State state) 
    throws SymbolicValueNotAllowedException, UndefinedResultException, FrozenStateException, InterruptException {
        //gets and checks the object parameter
        final Reference objRef = (Reference) this.data.operand(1);
        if (objRef instanceof KlassPseudoReference) {
        	this.obj = state.getKlass(((KlassPseudoReference) objRef).getClassFile());
        } else if (state.isNull(objRef)) {
            throw new UndefinedResultException("The object parameter to sun.misc.Unsafe.putLong[Volatile] was null.");
        } else {
        	this.obj = state.getObject(objRef);
        }
        if (this.obj == null) {
            throw new UnexpectedInternalException("Unexpected unresolved symbolic reference on the operand stack while invoking sun.misc.Unsafe.putLong[Volatile].");
        }

        //gets and checks the offset parameter
        final Primitive ofstPrimitive = (Primitive) this.data.operand(2);
        if (ofstPrimitive instanceof Simplex) {
            this.ofst = ((Long) ((Simplex) ofstPrimitive).getActualValue()).intValue();
        } else {
            throw new SymbolicValueNotAllowedException("The offset parameter to sun.misc.Unsafe.putLong[Volatile] cannot be a symbolic value.");
        }

        //checks
        if (!this.obj.hasOffset(this.ofst)) {
            throw new UndefinedResultException("The offset parameter to sun.misc.Unsafe.putLong[Volatile] was not a slot number of the object parameter.");
        }

        if (this.obj instanceof Array) {
            continueWith(this.algoArray);
        }
    }
    
    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            final Value val = this.data.operand(3);
            this.obj.setFieldValue(this.ofst, val);
        };
    }
}
