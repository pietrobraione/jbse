package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.continueWith;
import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.throwVerifyError;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Array;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.KlassPseudoReference;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Simplex;
import jbse.val.Value;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#getInt(Object, long)},
 * {@link sun.misc.Unsafe#getIntVolatile(Object, long)}, {@link sun.misc.Unsafe#getLong(Object, long)}
 * and {@link sun.misc.Unsafe#getLongVolatile(Object, long)}, {@link sun.misc.Unsafe#getObject(Object, long)} and
 * {@link sun.misc.Unsafe#getObjectVolatile(Object, long)}.
 * 
 * @author Pietro Braione
 */
public abstract class Algo_SUN_UNSAFE_GETX_O extends Algo_INVOKEMETA_Nonbranching {
    private final Algo_SUN_UNSAFE_GETX_O_Array algoArray; //set by constructor
    private final String methodName;                      //set by constructor
    private final String valueCheckErrorMessage;          //set by constructor
    private Value read;                                   //set by cookMore
    
    public Algo_SUN_UNSAFE_GETX_O(Algo_SUN_UNSAFE_GETX_O_Array algoArray, String methodName, String valueCheckErrorMessage) {
    	this.algoArray = algoArray;
    	this.methodName = methodName;
    	this.valueCheckErrorMessage = valueCheckErrorMessage;
    }

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 3;
    }
    
    protected abstract boolean valueTypeCorrect(char valueType);

    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException, FrozenStateException {
        try {           
            //gets and checks the object parameter
            final Reference objRef = (Reference) this.data.operand(1);
            final Objekt obj;
            if (objRef instanceof KlassPseudoReference) {
            	obj = state.getKlass(((KlassPseudoReference) objRef).getClassFile());
            } else if (state.isNull(objRef)) {
                throw new UndefinedResultException("The object parameter to sun.misc.Unsafe." + this.methodName + "[Volatile] was null");
            } else {
            	obj = state.getObject(objRef);
            }
            if (obj == null) {
                throw new UnexpectedInternalException("Unexpected unresolved symbolic reference on the operand stack while invoking sun.misc.Unsafe." + this.methodName + "[Volatile]");
            }

            //gets and checks the offset parameter
            final Primitive ofstPrimitive = (Primitive) this.data.operand(2);
            final int ofst;
            if (ofstPrimitive instanceof Simplex) {
                ofst = ((Long) ((Simplex) ofstPrimitive).getActualValue()).intValue();
            } else {
                throw new SymbolicValueNotAllowedException("The offset parameter to sun.misc.Unsafe." + this.methodName + "[Volatile] cannot be a symbolic value");
            }

            if (obj instanceof Array) {
                continueWith(this.algoArray);
            }
            
            //reads the value
            if (obj.hasOffset(ofst)) {
                this.read = obj.getFieldValue(ofst);
            } else {
                throw new UndefinedResultException("The offset parameter to sun.misc.Unsafe." + this.methodName + "[Volatile] was not a slot number of the object parameter");
            }
            
            //checks the value
            if (!valueTypeCorrect(this.read.getType())) {
                throw new UndefinedResultException("The value read by sun.misc.Unsafe." + this.methodName + "[Volatile] was not a" + this.valueCheckErrorMessage);
            }
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.read);
        };
    }
}
