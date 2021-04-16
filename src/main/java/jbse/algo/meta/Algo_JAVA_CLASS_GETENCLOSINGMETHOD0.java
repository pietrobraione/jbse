package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.algo.Util.invokeClassLoaderLoadClass;
import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.CannotManageStateException;
import jbse.bc.ClassFile;
import jbse.bc.Signature;
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
import jbse.val.ReferenceConcrete;
import jbse.val.Value;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link java.lang.Class#getEnclosingMethod0()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASS_GETENCLOSINGMETHOD0 extends Algo_INVOKEMETA_Nonbranching {
    private Value toPush; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException, InvalidInputException, 
    RenameUnsupportedException {
    	final Calculator calc = this.ctx.getCalculator();
        try {           
            //gets the classfile represented by the 'this' parameter
            final Reference classRef = (Reference) this.data.operand(0);
            final Instance_JAVA_CLASS clazz = (Instance_JAVA_CLASS) state.getObject(classRef); //TODO check that operand is concrete and not null
            final ClassFile cf = clazz.representedClass();
            final Signature sigEnclosing = cf.getEnclosingMethodOrConstructor();
            if (sigEnclosing == null) {
                this.toPush = Null.getInstance();
            } else {
                //resolves the enclosing class
                final ClassFile enclosingClass = state.getClassHierarchy().resolveClass(cf, sigEnclosing.getClassName(), state.bypassStandardLoading());
                
                //ensures the java.lang.Class of the enclosing class
                state.ensureInstance_JAVA_CLASS(calc, enclosingClass);
                
                //gets the (possibly null) descriptor and name of the enclosing method
                final Reference refDescriptor, refName;
                if (sigEnclosing.getName() == null || sigEnclosing.getDescriptor() == null) {
                    refDescriptor = refName = Null.getInstance();
                } else {
                    state.ensureStringLiteral(calc, sigEnclosing.getDescriptor());
                    refDescriptor = state.referenceToStringLiteral(sigEnclosing.getDescriptor());
                    state.ensureStringLiteral(calc, sigEnclosing.getName());
                    refName = state.referenceToStringLiteral(sigEnclosing.getName());
                }
                
                //creates the array
                final ClassFile cf_arrayOfJAVA_OBJECT = state.getClassHierarchy().loadCreateClass("" + ARRAYOF + REFERENCE + JAVA_OBJECT + TYPEEND);
                final ReferenceConcrete arrayRef = state.createArray(calc, null, calc.valInt(3), cf_arrayOfJAVA_OBJECT);
                this.toPush = arrayRef;
                final Array array = (Array) state.getObject(arrayRef);
                array.setFast(calc.valInt(0), state.referenceToInstance_JAVA_CLASS(enclosingClass));
                array.setFast(calc.valInt(1), refName);
                array.setFast(calc.valInt(2), refDescriptor);
            }
        } catch (PleaseLoadClassException e) {
            invokeClassLoaderLoadClass(state, calc, e);
            exitFromAlgorithm();
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, calc, OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        } catch (InvalidTypeException | ClassFileNotFoundException | 
                 ClassFileNotAccessibleException | IncompatibleClassFileException | 
                 ClassFileIllFormedException | BadClassFileVersionException | 
                 WrongClassNameException | FastArrayAccessNotAllowedException e) {
            //this should never happen
            failExecution(e);
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.toPush);
        };
    }
}
