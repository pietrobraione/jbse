package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.bc.Modifiers.isStatic;
import static jbse.bc.Signatures.ILLEGAL_ARGUMENT_EXCEPTION;
import static jbse.bc.Signatures.JAVA_FIELD_CLAZZ;
import static jbse.bc.Signatures.JAVA_FIELD_MODIFIERS;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.ClassFile;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Instance;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Simplex;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

public final class Algo_SUN_UNSAFE_STATICFIELDBASE extends Algo_INVOKEMETA_Nonbranching {
	private Reference base; //set by cookMore
	
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException, FrozenStateException, 
    InvalidTypeException, InvalidOperandException {
    	final Calculator calc = this.ctx.getCalculator();
        try {
            //gets the field object
            final Reference fldRef = (Reference) this.data.operand(1);
            if (state.isNull(fldRef)) {
                throwNew(state, calc, NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }
            final Instance fldInstance = (Instance) state.getObject(fldRef);
            if (fldInstance.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The Field f parameter to invocation of method sun.misc.Unsafe.staticFieldBase must not be symbolic.");
            }
            
            //gets the modifiers of the field
            final Primitive primitiveModifiers = (Primitive) fldInstance.getFieldValue(JAVA_FIELD_MODIFIERS);
            if (fldInstance.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The int f.modifiers field to invocation of method sun.misc.Unsafe.staticFieldBase must not be symbolic.");
            }
            final int modifiers = ((Integer) ((Simplex) primitiveModifiers).getActualValue()).intValue();
            if (!isStatic(modifiers)) {
                throwNew(state, calc, ILLEGAL_ARGUMENT_EXCEPTION);
                exitFromAlgorithm();
            }
            
            //gets the class of the field
            final Reference fldClassInstanceRef = (Reference) fldInstance.getFieldValue(JAVA_FIELD_CLAZZ);
            final Instance_JAVA_CLASS fldClassInstance = (Instance_JAVA_CLASS) state.getObject(fldClassInstanceRef);
            final ClassFile fldClassFile = fldClassInstance.representedClass();
            
            //gets the origin reference of the corresponding Klass object
            this.base = state.getKlass(fldClassFile).getOrigin();
        } catch (ClassCastException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.base);
        };
    }
}
