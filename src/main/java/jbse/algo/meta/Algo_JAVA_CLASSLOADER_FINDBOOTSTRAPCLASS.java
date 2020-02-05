package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.UNSUPPORTED_CLASS_VERSION_ERROR;
import static jbse.common.Type.internalClassName;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
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
import jbse.mem.State;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Null;
import jbse.val.Reference;

/**
 * Meta-level implementation of {@link java.lang.ClassLoader#findBootstrapClass(String)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASSLOADER_FINDBOOTSTRAPCLASS extends Algo_INVOKEMETA_Nonbranching {
    private Reference classRef; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected void cookMore(State state) 
    throws ThreadStackEmptyException, ClasspathException, SymbolicValueNotAllowedException, 
    InterruptException, InvalidInputException, RenameUnsupportedException {
    	final Calculator calc = this.ctx.getCalculator();
        try {
            //gets the name of the class
            final Reference classNameRef = (Reference) this.data.operand(1);
            if (state.isNull(classNameRef)) {
                this.classRef = Null.getInstance();
                return;
                
            }
            final String className = internalClassName(valueString(state, classNameRef));
            if (className == null) {
                throw new SymbolicValueNotAllowedException("The className parameter to java.lang.ClassLoader.findBootstrapClass cannot be a symbolic String");
            }
            
            //looks for the class
            final ClassFile classFile = state.getClassHierarchy().loadCreateClass(className);
            state.ensureInstance_JAVA_CLASS(calc, classFile);
            this.classRef = state.referenceToInstance_JAVA_CLASS(classFile);
        } catch (ClassFileNotFoundException e) {
            this.classRef = Null.getInstance();
        } catch (BadClassFileVersionException e) {
            throwNew(state, calc, UNSUPPORTED_CLASS_VERSION_ERROR);
            exitFromAlgorithm();
        } catch (WrongClassNameException e) {
            throwNew(state, calc, NO_CLASS_DEFINITION_FOUND_ERROR); //without wrapping a ClassNotFoundException
            exitFromAlgorithm();
        } catch (IncompatibleClassFileException e) {
            throwNew(state, calc, INCOMPATIBLE_CLASS_CHANGE_ERROR);
            exitFromAlgorithm();
        } catch (ClassFileNotAccessibleException e) {
            throwNew(state, calc, ILLEGAL_ACCESS_ERROR);
            exitFromAlgorithm();
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, calc, OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (ClassFileIllFormedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassCastException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        }
    }

    @Override
    protected final StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.classRef);
        };
    }
}
