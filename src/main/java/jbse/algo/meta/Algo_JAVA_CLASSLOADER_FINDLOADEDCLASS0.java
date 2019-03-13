package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.common.Type.internalClassName;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.ClassFile;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.Instance_JAVA_CLASSLOADER;
import jbse.mem.State;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Null;
import jbse.val.Reference;

/**
 * Meta-level implementation of {@link java.lang.ClassLoader#findLoadedClass0(String)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASSLOADER_FINDLOADEDCLASS0 extends Algo_INVOKEMETA_Nonbranching {
    private Reference classRef; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected void cookMore(State state) 
    throws ThreadStackEmptyException, ClasspathException, 
    SymbolicValueNotAllowedException, InterruptException, InvalidInputException {
    	final Calculator calc = this.ctx.getCalculator();
        try {
            //gets the classloader ('this' object)
            final Reference classLoaderRef = (Reference) this.data.operand(0);
            if (state.isNull(classLoaderRef)) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.ClassLoader.findLoadedClass0 method is null.");
            }
            final Instance_JAVA_CLASSLOADER classLoaderObject = (Instance_JAVA_CLASSLOADER) state.getObject(classLoaderRef);
            final int classLoader = classLoaderObject.classLoaderIdentifier();
            
            //gets the name of the class
            final Reference classNameRef = (Reference) this.data.operand(1);
            if (state.isNull(classNameRef)) {
                this.classRef = Null.getInstance();
                return;
                
            }
            final String className = internalClassName(valueString(state, classNameRef));
            if (className == null) {
                throw new SymbolicValueNotAllowedException("The className parameter to java.lang.ClassLoader.findLoadedClass0 cannot be a symbolic String");
            }
            
            //looks for the class
            final ClassFile classFile = state.getClassHierarchy().getClassFileClassArray(classLoader, className);
            if (classFile == null) {
                this.classRef = Null.getInstance();
            } else {
                state.ensureInstance_JAVA_CLASS(calc, classFile);
                this.classRef = state.referenceToInstance_JAVA_CLASS(classFile);
            }
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, calc, OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
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
