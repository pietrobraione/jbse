package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;
import static jbse.bc.Signatures.ILLEGAL_ARGUMENT_EXCEPTION;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;
import static jbse.common.Type.binaryPrimitiveClassNameToInternal;
import static jbse.common.Type.isPrimitiveBinaryClassName;

import java.util.function.Supplier;

import jbse.algo.Algo_XNEWARRAY;
import jbse.algo.Algorithm;
import jbse.algo.BytecodeData_1ZME;
import jbse.algo.InterruptException;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.val.Primitive;
import jbse.val.Reference;

/**
 * Meta-level implementation of {@link java.lang.reflect.Array#newArray(Class, int)}.
 * This {@link Algorithm} completes the execution.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_REFLECT_ARRAY_NEWARRAY_COMPLETION extends Algo_XNEWARRAY<BytecodeData_1ZME> {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }
    
    @Override
    protected final Supplier<BytecodeData_1ZME> bytecodeData() {
        return () -> BytecodeData_1ZME.withInterfaceMethod(false).get();
    }

    @Override
    protected void preCook(State state) throws InterruptException {
        //sets the array length
        try {
            this.dimensionsCounts = new Primitive[] { (Primitive) this.data.operand(1) };
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }

        //sets the array type
        try {
            final Reference refToClass = (Reference) this.data.operand(0);
            if (state.isNull(refToClass)) {
                throwNew(state, NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }
            //TODO the next cast fails if javaClassRef is symbolic and expanded to a regular Instance. Handle the case.
            final Instance_JAVA_CLASS clazz = (Instance_JAVA_CLASS) state.getObject(refToClass);
            if (clazz == null) {
                //this should never happen
                failExecution("The first parameter to java.lang.reflect.Array.newArray method is symbolic and unresolved.");
            }
            final Reference refToVoidClass = state.referenceToInstance_JAVA_CLASS_primitive("void");
            if (refToVoidClass != null) {
                final Instance_JAVA_CLASS voidClass = (Instance_JAVA_CLASS) state.getObject(refToVoidClass);
                if (clazz == voidClass) {
                    throwNew(state, ILLEGAL_ARGUMENT_EXCEPTION);
                    exitFromAlgorithm();
                }
            }
            if (isPrimitiveBinaryClassName(clazz.representedClass())) {
                this.arrayType = "" + ARRAYOF + binaryPrimitiveClassNameToInternal(clazz.representedClass());
            } else {
                this.arrayType = "" + ARRAYOF + REFERENCE + clazz.representedClass() + TYPEEND;
            }
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }
    }
    
    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> INVOKESPECIALSTATICVIRTUAL_OFFSET;
    }
}
