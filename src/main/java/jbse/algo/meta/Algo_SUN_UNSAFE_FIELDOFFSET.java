package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.bc.Modifiers.isStatic;
import static jbse.bc.Signatures.ILLEGAL_ARGUMENT_EXCEPTION;
import static jbse.bc.Signatures.INTERNAL_ERROR;
import static jbse.bc.Signatures.JAVA_FIELD_CLAZZ;
import static jbse.bc.Signatures.JAVA_FIELD_MODIFIERS;
import static jbse.bc.Signatures.JAVA_FIELD_SLOT;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;
import static jbse.common.Type.LONG;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.ClassFile;
import jbse.bc.Signature;
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

abstract class Algo_SUN_UNSAFE_FIELDOFFSET extends Algo_INVOKEMETA_Nonbranching {
	private final boolean fieldMustBeStatic; //set by constructor
	private Simplex ofst; //set by cookMore
	
	Algo_SUN_UNSAFE_FIELDOFFSET(boolean fieldMustBeStatic) {
		this.fieldMustBeStatic = fieldMustBeStatic;
	}

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
                throw new SymbolicValueNotAllowedException("The Field f parameter to invocation of method sun.misc.Unsafe.objectFieldOffset must not be symbolic.");
            }
            
            //gets the modifiers of the field
            final Primitive primitiveModifiers = (Primitive) fldInstance.getFieldValue(JAVA_FIELD_MODIFIERS);
            if (fldInstance.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The int f.modifiers field to invocation of method sun.misc.Unsafe.objectFieldOffset must not be symbolic.");
            }
            final int modifiers = ((Integer) ((Simplex) primitiveModifiers).getActualValue()).intValue();
            if (this.fieldMustBeStatic != isStatic(modifiers)) {
                throwNew(state, calc, ILLEGAL_ARGUMENT_EXCEPTION);
                exitFromAlgorithm();
            }
            
            //gets the class of the field
            final Reference fldClassInstanceRef = (Reference) fldInstance.getFieldValue(JAVA_FIELD_CLAZZ);
            final Instance_JAVA_CLASS fldClassInstance = (Instance_JAVA_CLASS) state.getObject(fldClassInstanceRef);
            final ClassFile fldClassFile = fldClassInstance.representedClass();
            
            //gets the slot
            final Simplex _slot = (Simplex) fldInstance.getFieldValue(JAVA_FIELD_SLOT);
            if (_slot.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The slot field in a java.lang.reflect.Field object is symbolic.");
            }
            final int slot = ((Integer) ((Simplex) _slot).getActualValue()).intValue();
            
            //gets the field signature
            final Signature[] declaredFields = fldClassFile.getDeclaredFields();
            if (slot < 0 || slot >= declaredFields.length) {
                //invalid slot number
                throwNew(state, calc, INTERNAL_ERROR);
                exitFromAlgorithm();
            }            
            final Signature fieldSignature = declaredFields[slot];
            
            //gets the offset
            final int _ofst = fldClassFile.getFieldOffset(fieldSignature);
            if (_ofst == -1) {
                //this should never happen
                failExecution("Declared field " + fieldSignature + " not found in the list of all fields of an object with class " + fldClassFile.getClassName() + ".");
            }
            this.ofst = (Simplex) calc.pushInt(_ofst).to(LONG).pop();
        } catch (ClassCastException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        }
        //TODO check that operands are concrete and kill path if they are not
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.ofst);
        };
    }
}
