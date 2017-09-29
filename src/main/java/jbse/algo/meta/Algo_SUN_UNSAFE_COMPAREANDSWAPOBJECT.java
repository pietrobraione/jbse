package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwVerifyError;
import static jbse.mem.Util.areAlias;
import static jbse.mem.Util.areNotAlias;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;
import jbse.val.Simplex;

public final class Algo_SUN_UNSAFE_COMPAREANDSWAPOBJECT extends Algo_INVOKEMETA_Nonbranching {
    private Objekt objectToSet; //set by cookMore
    private int fieldSlotToSet; //set by cookMore
    private Reference toWrite; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 5;
    }
    
    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException {
        try {
            final Reference refObjectToSet = (Reference) this.data.operand(1);
            this.objectToSet = state.getObject(refObjectToSet);
            if (this.objectToSet == null) {
                throw new SymbolicValueNotAllowedException("the object to be set parameter to sun.misc.Unsafe.CompareAndSwapObject must be concrete or resolved symbolic");
            }
            if (this.data.operand(2) instanceof Simplex) {
                this.fieldSlotToSet = ((Long) ((Simplex) this.data.operand(2)).getActualValue()).intValue();
            } else {
                throw new SymbolicValueNotAllowedException("the slot number parameter to sun.misc.Unsafe.CompareAndSwapObject must be concrete or resolved symbolic");
            }
            final Reference toCompare = (Reference) this.data.operand(3);
            this.toWrite = (Reference) this.data.operand(4);
            final Reference current = (Reference) this.objectToSet.getFieldValue(this.fieldSlotToSet);
            if (areNotAlias(state, current, toCompare)) {
                this.objectToSet = null;
            } else if (!areAlias(state, current, toCompare)) {
                throw new SymbolicValueNotAllowedException("the references to be compared during an invocation to sun.misc.Unsafe.CompareAndSwapObject must be concrete or resolved symbolic");
            }
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }        
    }
    
    
    @Override
    protected void update(State state) throws ThreadStackEmptyException {
        if (this.objectToSet == null) {
            state.pushOperand(state.getCalculator().valBoolean(false));
        } else {
            this.objectToSet.setFieldValue(this.fieldSlotToSet, this.toWrite);
            state.pushOperand(state.getCalculator().valBoolean(true));
        }
    }
}
