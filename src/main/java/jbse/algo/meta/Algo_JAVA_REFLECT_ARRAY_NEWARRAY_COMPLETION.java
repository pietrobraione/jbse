package jbse.algo.meta;

import static jbse.algo.BytecodeData_1KME.Kind.kind;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.invokeClassLoaderLoadClass;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;
import static jbse.bc.Signatures.ILLEGAL_ARGUMENT_EXCEPTION;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;
import static jbse.common.Type.ARRAYOF;

import java.util.function.Supplier;

import jbse.algo.Algo_XNEWARRAY;
import jbse.algo.Algorithm;
import jbse.algo.BytecodeData_1KME;
import jbse.algo.InterruptException;
import jbse.bc.ClassFile;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.Reference;

/**
 * Meta-level implementation of {@link java.lang.reflect.Array#newArray(Class, int)}.
 * This {@link Algorithm} completes the execution.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_REFLECT_ARRAY_NEWARRAY_COMPLETION extends Algo_XNEWARRAY<BytecodeData_1KME> {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected final Supplier<BytecodeData_1KME> bytecodeData() {
        return () -> BytecodeData_1KME.withMethod(kind(false, false, true)).get();
    }

    @Override
    protected void preCook(State state) 
    throws InterruptException, ClasspathException, InvalidInputException, 
    ThreadStackEmptyException, RenameUnsupportedException {
    	final Calculator calc = this.ctx.getCalculator();
    	
        //sets the array length
        try {
            this.dimensionsCounts = new Primitive[] { (Primitive) this.data.operand(1) };
        } catch (ClassCastException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        }

        //sets the array type
        try {
            final Reference refToClass = (Reference) this.data.operand(0);
            if (state.isNull(refToClass)) {
                throwNew(state, calc, NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }
            //TODO the next cast fails if javaClassRef is symbolic and expanded to a regular Instance. Handle the case.
            final Instance_JAVA_CLASS clazz = (Instance_JAVA_CLASS) state.getObject(refToClass);
            if (clazz == null) {
                //this should never happen
                failExecution("the first parameter to java.lang.reflect.Array.newArray method is symbolic and unresolved");
            }
            final Reference refToVoidClass = state.referenceToInstance_JAVA_CLASS_primitiveOrVoid("void");
            if (refToVoidClass != null) {
                final Instance_JAVA_CLASS voidClass = (Instance_JAVA_CLASS) state.getObject(refToVoidClass);
                if (clazz == voidClass) {
                    throwNew(state, calc, ILLEGAL_ARGUMENT_EXCEPTION);
                    exitFromAlgorithm();
                }
            }
            final ClassFile arrayMemberType = clazz.representedClass();
            final String arrayTypeName = "" + ARRAYOF + arrayMemberType.getInternalTypeName();
            this.arrayType = state.getClassHierarchy().loadCreateClass(arrayMemberType.getDefiningClassLoader(), arrayTypeName, state.bypassStandardLoading());
        } catch (PleaseLoadClassException e) {
            invokeClassLoaderLoadClass(state, calc, e);
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        } catch (ClassFileNotFoundException | ClassFileIllFormedException | BadClassFileVersionException |
                 WrongClassNameException | IncompatibleClassFileException | ClassFileNotAccessibleException e) {
            //this should never happen
            failExecution(e);
        }
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> INVOKESPECIALSTATICVIRTUAL_OFFSET;
    }
}
