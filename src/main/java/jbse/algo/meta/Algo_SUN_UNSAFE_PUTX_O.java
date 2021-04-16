package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.continueWith;

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
 * Meta-level implementation of {@link sun.misc.Unsafe#putInt(Object, long, int)}, 
 * {@link sun.misc.Unsafe#putIntVolatile(Object, long, int)}, {@link sun.misc.Unsafe#putLong(Object, long, long)}, 
 * {@link sun.misc.Unsafe#putLongVolatile(Object, long, long)}, {@link sun.misc.Unsafe#putObject(Object, long, Object)}, 
 * {@link sun.misc.Unsafe#putObjectVolatile(Object, long, Object)} and 
 * {@link sun.misc.Unsafe#putOrderedObject(Object, long, Object)}
 * 
 * @author Pietro Braione
 */
public abstract class Algo_SUN_UNSAFE_PUTX_O extends Algo_INVOKEMETA_Nonbranching {
	private final Algo_SUN_UNSAFE_PUTX_O_Array algoArray; //set by constructor
	private final String methodName; //set by constructor
	private Objekt obj; //set by cookMore
	private int ofst;   //set by cookMore
	
	public Algo_SUN_UNSAFE_PUTX_O(Algo_SUN_UNSAFE_PUTX_O_Array algoArray, String methodName) {
		this.algoArray = algoArray;
		this.methodName = methodName;
	}
	
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
            throw new UndefinedResultException("The Object o parameter to sun.misc.Unsafe." + this.methodName + "[Volatile] was null.");
        } else {
        	this.obj = state.getObject(objRef);
        }
        if (this.obj == null) {
            throw new UnexpectedInternalException("Unexpected unresolved symbolic reference on the operand stack while invoking sun.misc.Unsafe." + this.methodName + "[Volatile].");
        }

        //gets and checks the offset parameter
        final Primitive ofstPrimitive = (Primitive) this.data.operand(2);
        if (ofstPrimitive instanceof Simplex) {
            this.ofst = ((Long) ((Simplex) ofstPrimitive).getActualValue()).intValue();
        } else {
            throw new SymbolicValueNotAllowedException("The long offset parameter to sun.misc.Unsafe." + this.methodName + "[Volatile] cannot be a symbolic value.");
        }

        //checks
        if (!this.obj.hasOffset(this.ofst)) {
            throw new UndefinedResultException("The long offset parameter to sun.misc.Unsafe." + this.methodName + "[Volatile] was not a slot number of the Object o parameter.");
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
