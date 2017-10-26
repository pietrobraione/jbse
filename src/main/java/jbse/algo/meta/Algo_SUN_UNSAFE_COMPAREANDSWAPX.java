package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwVerifyError;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.NotYetImplementedException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Array;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;
import jbse.val.Simplex;
import jbse.val.Value;

public abstract class Algo_SUN_UNSAFE_COMPAREANDSWAPX extends Algo_INVOKEMETA_Nonbranching {
    private final String what;  //set by constructor
    private Objekt objectToSet; //set by cookMore
    private int fieldSlotToSet; //set by cookMore
    private Value toWrite; //set by cookMore

    protected Algo_SUN_UNSAFE_COMPAREANDSWAPX(String what) {
        this.what = what;
    }

    @Override
    protected final Supplier<Integer> numOperands() {
        return () -> 5;
    }

    protected abstract boolean checkCompare(State state, Value current, Value toCompare) 
    throws CannotManageStateException, InterruptException;

    @Override
    protected final void cookMore(State state)
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException {
        try {
            final Reference refObjectToSet = (Reference) this.data.operand(1);
            this.objectToSet = state.getObject(refObjectToSet);
            if (this.objectToSet == null) {
                throw new SymbolicValueNotAllowedException("The object to be set parameter to sun.misc.Unsafe.CompareAndSwap" + this.what + " must be concrete or resolved symbolic");
            }
            if (this.objectToSet instanceof Array) {
                throw new NotYetImplementedException("Unclear whether sun.misc.Unsafe.CompareAndSwap" + this.what + " can be applied to an array");
            }
            if (this.data.operand(2) instanceof Simplex) {
                this.fieldSlotToSet = ((Long) ((Simplex) this.data.operand(2)).getActualValue()).intValue();
            } else {
                throw new SymbolicValueNotAllowedException("The slot parameter to sun.misc.Unsafe.CompareAndSwap" + this.what + " must be concrete or resolved symbolic");
            }
            final Value toCompare = this.data.operand(3);
            this.toWrite = this.data.operand(4);
            if (this.objectToSet.hasSlot(this.fieldSlotToSet)) {
                final Value current = this.objectToSet.getFieldValue(this.fieldSlotToSet);
                if (!checkCompare(state, current, toCompare)) {
                    this.objectToSet = null;
                }
            } else {
                throw new UndefinedResultException("The offset parameter to sun.misc.Unsafe.CompareAndSwap" + this.what + " was not a slot number of the object parameter");
            }
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }        
    }


    @Override
    protected final void update(State state) throws ThreadStackEmptyException {
        if (this.objectToSet == null) {
            state.pushOperand(state.getCalculator().valInt(0)); //false
        } else {
            this.objectToSet.setFieldValue(this.fieldSlotToSet, this.toWrite);
            state.pushOperand(state.getCalculator().valInt(1)); //true
        }
    }
}
