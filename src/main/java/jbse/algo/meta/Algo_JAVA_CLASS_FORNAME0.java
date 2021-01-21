package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.invokeClassLoaderLoadClass;
import static jbse.algo.Util.ensureClassInitialized;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.bc.ClassLoaders.CLASSLOADER_BOOT;
import static jbse.bc.Signatures.CLASS_NOT_FOUND_EXCEPTION;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.JBSE_BASE_BOXEXCEPTIONININITIALIZERERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.UNSUPPORTED_CLASS_VERSION_ERROR;
import static jbse.common.Type.internalClassName;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
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
import jbse.dec.exc.DecisionException;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.Instance_JAVA_CLASSLOADER;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.lang.Class#forName0(String, boolean, ClassLoader, Class)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASS_FORNAME0 extends Algo_INVOKEMETA_Nonbranching {
    private Reference classRef; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 4;
    }

    @Override
    protected void cookMore(State state) 
    throws ThreadStackEmptyException, DecisionException, 
    ClasspathException, SymbolicValueNotAllowedException, 
    InvalidInputException, InterruptException, 
    ContradictionException, RenameUnsupportedException {
        try {
            //gets the name of the class
            final Reference classNameRef = (Reference) this.data.operand(0);
            if (state.isNull(classNameRef)) {
                throwNew(state, this.ctx.getCalculator(), NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }
            final String className = internalClassName(valueString(state, classNameRef));
            if (className == null) {
                throw new SymbolicValueNotAllowedException("The className parameter to java.lang.Class.forName0 cannot be a symbolic String");
            }
            
            //gets whether the class must be initialized
            if (!(this.data.operand(1) instanceof Primitive)) {
                throwVerifyError(state, this.ctx.getCalculator());
                exitFromAlgorithm();
            } else if (!(this.data.operand(1) instanceof Simplex)) {
                throw new SymbolicValueNotAllowedException("The toInit parameter to java.lang.Class.forName0 cannot be a symbolic boolean");
            }
            final boolean toInit = (((Integer) ((Simplex) this.data.operand(1)).getActualValue()).intValue() > 0);
            
            //gets the classloader
            final Reference classLoaderRef = (Reference) this.data.operand(2);
            final int classLoader;
            if (state.isNull(classLoaderRef)) {
                classLoader = CLASSLOADER_BOOT;
            } else {
                final Instance_JAVA_CLASSLOADER classLoaderInstance = (Instance_JAVA_CLASSLOADER) state.getObject(classLoaderRef);
                classLoader = classLoaderInstance.classLoaderIdentifier();
            }
            
            //loads/creates the class
            final ClassHierarchy hier = state.getClassHierarchy();
            final ClassFile classFile = hier.loadCreateClass(classLoader, className, state.bypassStandardLoading());
            
            //gets the caller class
            final Reference callerClassRef = (Reference) this.data.operand(3);
            final ClassFile callerClass;
            if (state.isNull(callerClassRef)) {
                callerClass = classFile; //free access!
            } else  {
                final Instance_JAVA_CLASS callerClassObject = (Instance_JAVA_CLASS) state.getObject(callerClassRef);
                callerClass = callerClassObject.representedClass();
            }

            //checks whether callerClass can access this.classFile 
            if (!hier.isClassAccessible(callerClass, classFile)) {
                throwNew(state, this.ctx.getCalculator(), ILLEGAL_ACCESS_ERROR);
                exitFromAlgorithm();
            }
            
            //makes the instance of java.lang.Class and possibly
            //initializes it
            state.ensureInstance_JAVA_CLASS(this.ctx.getCalculator(), classFile);
            if (toInit) {
                ensureClassInitialized(state, this.ctx, JBSE_BASE_BOXEXCEPTIONININITIALIZERERROR, classFile); 
            }
            
            this.classRef = state.referenceToInstance_JAVA_CLASS(classFile);
        } catch (PleaseLoadClassException e) {
            invokeClassLoaderLoadClass(state, this.ctx.getCalculator(), e);
            exitFromAlgorithm();
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, this.ctx.getCalculator(), OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (ClassFileNotFoundException e) {
            throwNew(state, this.ctx.getCalculator(), CLASS_NOT_FOUND_EXCEPTION);
            exitFromAlgorithm();
        } catch (BadClassFileVersionException e) {
            throwNew(state, this.ctx.getCalculator(), UNSUPPORTED_CLASS_VERSION_ERROR);
            exitFromAlgorithm();
        } catch (WrongClassNameException e) {
            throwNew(state, this.ctx.getCalculator(), NO_CLASS_DEFINITION_FOUND_ERROR); //without wrapping a ClassNotFoundException
            exitFromAlgorithm();
        } catch (ClassFileNotAccessibleException e) {
            throwNew(state, this.ctx.getCalculator(), ILLEGAL_ACCESS_ERROR);
            exitFromAlgorithm();
        } catch (IncompatibleClassFileException e) {
            throwNew(state, this.ctx.getCalculator(), INCOMPATIBLE_CLASS_CHANGE_ERROR);
            exitFromAlgorithm();
        } catch (ClassFileIllFormedException | ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
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
