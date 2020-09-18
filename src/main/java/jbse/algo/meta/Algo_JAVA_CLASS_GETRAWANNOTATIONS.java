package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.BYTE;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.CannotManageStateException;
import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Array;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Null;
import jbse.val.Reference;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link java.lang.Class#getRawAnnotations()}.
 *  
 * @author Pietro Braione
 *
 */
public final class Algo_JAVA_CLASS_GETRAWANNOTATIONS extends Algo_INVOKEMETA_Nonbranching {
    private Reference annotationsRef;  //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException, InvalidInputException, 
    RenameUnsupportedException {
        try {
            //gets the classfile
            final Reference classRef = (Reference) this.data.operand(0);
            if (state.isNull(classRef)) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Class.getRawAnnotations method is null.");
            }
            final Instance_JAVA_CLASS clazz = (Instance_JAVA_CLASS) state.getObject(classRef);
            final ClassFile classFile = clazz.representedClass();
            
            //if the class is primitive or array, then
            //the value to push is null (as resulting from
            //Hotspost source code comments)
            if (classFile.isPrimitiveOrVoid() || classFile.isArray()) {
                this.annotationsRef = Null.getInstance();
                return;
            }
            
            //gets the (raw) annotations
            final byte[] annotations = classFile.getClassAnnotationsRaw();
            
            //if the annotations array is empty, we need to push null
            //(an empty array misses metadata, and this confuses reflection
            //methods, that therefore expect null)
            if (annotations.length == 0) {
            	this.annotationsRef = Null.getInstance();
                return;
            }
            
            //creates the destination array
            final ClassHierarchy hier = state.getClassHierarchy();
            final Calculator calc = this.ctx.getCalculator();
            final ClassFile cf_arrayOfBYTE = hier.loadCreateClass("" + ARRAYOF + BYTE);
            this.annotationsRef = state.createArray(calc, null, calc.valInt(annotations.length), cf_arrayOfBYTE);

            //populates the destination array
            final Array annotationsArray = (Array) state.getObject(this.annotationsRef);
            for (int i = 0; i < annotations.length; ++i) {
                annotationsArray.setFast(calc.valInt(i), calc.valByte(annotations[i]));
            }
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, this.ctx.getCalculator(), OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        } catch (ClassFileNotFoundException | ClassFileIllFormedException| ClassFileNotAccessibleException |
                 IncompatibleClassFileException| BadClassFileVersionException | WrongClassNameException | 
                 InvalidTypeException | FastArrayAccessNotAllowedException e) {
            //this should never happen
            failExecution(e);
        }
    }

    @Override
    protected final StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.annotationsRef);
        };
    }
}
