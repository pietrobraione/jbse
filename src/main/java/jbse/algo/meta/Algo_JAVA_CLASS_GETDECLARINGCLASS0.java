package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.invokeClassLoaderLoadClass;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.CLASS_NOT_FOUND_EXCEPTION;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.UNSUPPORTED_CLASS_VERSION_ERROR;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
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
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Null;
import jbse.val.Reference;

/**
 * Meta-level implementation of {@link java.lang.Class#getDeclaringClass0()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASS_GETDECLARINGCLASS0 extends Algo_INVOKEMETA_Nonbranching {
    private Reference declaringClass;

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, InterruptException, ClasspathException, 
    InvalidInputException, RenameUnsupportedException {
    	final Calculator calc = this.ctx.getCalculator();
        //There are four kind of classes:
        //
        //1- Top-level: not nested (declared outside everything in packages, or so it seems)
        //2- Nested: declared inside another class/interface; among this there are static nested 
        //   (associated to the class) and inner (associated to an instance) 
        //3- Local: a particular kind of inner class, declared inside a block
        //4- Anonymous: a particular kind of local classes, they don't have a name
        //
        //By reversing the JVM code it seems that this method should return null
        //for top-level classes, local and anonymous classes, as these are not,
        //or are not considered, members of the class they are nested in.
        //Thus this method should return, only for static nested classes and 
        //proper (neither local nor anonymous) inner classes the 
        //container class.
        try {
            //gets the 'this' java.lang.Class instance from the heap 
            //and from it the class it represents as a ClassFile
            final Reference javaClassRef = (Reference) this.data.operand(0);
            if (state.isNull(javaClassRef)) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Class.getDeclaringClass0 method is null.");
            }
            //TODO the next cast fails if javaClassRef is symbolic and expanded to a regular Instance. Handle the case.
            final Instance_JAVA_CLASS javaClass = (Instance_JAVA_CLASS) state.getObject(javaClassRef);
            if (javaClass == null) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Class.getDeclaringClass0 method is symbolic and unresolved.");
            }
            final ClassFile thisClass = javaClass.representedClass();
            
            //gets a reference to the java.lang.Class object for the container class
            final String declaringClassName = thisClass.classContainer();
            if (declaringClassName == null || thisClass.isArray() || thisClass.isPrimitiveOrVoid() || thisClass.isLocal() || thisClass.isAnonymous()) {
                this.declaringClass = Null.getInstance();
            } else {
                final ClassFile declaringClassFile = state.getClassHierarchy().resolveClass(thisClass, declaringClassName, state.bypassStandardLoading()); //TODO is ok that accessor == thisClass?
                state.ensureInstance_JAVA_CLASS(calc, declaringClassFile);
                this.declaringClass = state.referenceToInstance_JAVA_CLASS(declaringClassFile);
            }            
        } catch (PleaseLoadClassException e) {
            invokeClassLoaderLoadClass(state, calc, e);
            exitFromAlgorithm();
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, calc, OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (ClassFileNotFoundException e) {
            throwNew(state, calc, CLASS_NOT_FOUND_EXCEPTION);
            exitFromAlgorithm();
        } catch (BadClassFileVersionException e) {
            throwNew(state, calc, UNSUPPORTED_CLASS_VERSION_ERROR);
            exitFromAlgorithm();
        } catch (WrongClassNameException e) {
            throwNew(state, calc, NO_CLASS_DEFINITION_FOUND_ERROR); //without wrapping a ClassNotFoundException
            exitFromAlgorithm();
        } catch (ClassFileNotAccessibleException e) {
            throwNew(state, calc, ILLEGAL_ACCESS_ERROR);
            exitFromAlgorithm();
        } catch (IncompatibleClassFileException e) {
            throwNew(state, calc, INCOMPATIBLE_CLASS_CHANGE_ERROR);
            exitFromAlgorithm();
        } catch (ClassFileIllFormedException e) {
            //TODO throw a LinkageError?
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        }
    }
    
    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.declaringClass);
        };
    }
}
