package jbse.algo.meta;

import static jbse.algo.Util.ensureClassInitialized;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.SUN_CONSTANTPOOL;
import static jbse.bc.Signatures.SUN_CONSTANTPOOL_CONSTANTPOOLOOP;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.CannotManageStateException;
import jbse.bc.ClassFile;
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
import jbse.mem.Instance;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Null;
import jbse.val.Reference;

/**
 * Meta-level implementation of {@link java.lang.Class#getConstantPool()}.
 *  
 * @author Pietro Braione
 *
 */
public final class Algo_JAVA_CLASS_GETCONSTANTPOOL extends Algo_INVOKEMETA_Nonbranching {
    private Reference toPush; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException, InvalidInputException, 
    ContradictionException, RenameUnsupportedException {
        try {
            //gets the classfile
            final Reference classRef = (Reference) this.data.operand(0);
            if (state.isNull(classRef)) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Class.getConstantPool method is null.");
            }
            final Instance_JAVA_CLASS clazz = (Instance_JAVA_CLASS) state.getObject(classRef);
            final ClassFile classFile = clazz.representedClass();
            
            //if the class is primitive or array, then
            //the value to push is null (as resulting from
            //Hotspost source code comments)
            if (classFile.isPrimitiveOrVoid() || classFile.isArray()) {
                this.toPush = Null.getInstance();
                return;
            }
            
            //loads and initializes the class sun.reflect.ConstantPool
            final ClassFile cf_SUN_CONSTANTPOOL = state.getClassHierarchy().loadCreateClass(SUN_CONSTANTPOOL);
            ensureClassInitialized(state, this.ctx, cf_SUN_CONSTANTPOOL);
            
            //creates the instance
            this.toPush = state.createInstance(this.ctx.getCalculator(), cf_SUN_CONSTANTPOOL);
            
            //sets the private constantPoolOop field
            //(we just set it to classRef, which is the easiest solution)
            final Instance constPoolInstance = (Instance) state.getObject(this.toPush);
            constPoolInstance.setFieldValue(SUN_CONSTANTPOOL_CONSTANTPOOLOOP, classRef);
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, this.ctx.getCalculator(), OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        } catch (ClassFileNotFoundException | ClassFileIllFormedException| ClassFileNotAccessibleException |
                 IncompatibleClassFileException| BadClassFileVersionException | WrongClassNameException e) {
            //this should never happen
            failExecution(e);
        }
    }

    @Override
    protected final StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.toPush);
        };
    }
}
