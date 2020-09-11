package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.JAVA_CLASS;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;

import java.util.List;
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
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.Array;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Reference;

/**
 * Meta-level implementation of {@link java.lang.Class#getInterfaces0()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASS_GETINTERFACES0 extends Algo_INVOKEMETA_Nonbranching {
    private Reference[] refSuperInterfaces; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, ClasspathException, InvalidInputException {
    	final Calculator calc = this.ctx.getCalculator();
        try {
            final Instance_JAVA_CLASS clazz = (Instance_JAVA_CLASS) state.getObject((Reference) this.data.operand(0));
            if (clazz == null) {
                //this should never happen
                failExecution("Violated invariant (unexpected heap access with symbolic unresolved reference).");
            }
            final ClassFile cf = clazz.representedClass();
            final List<ClassFile> superInterfaces = cf.getSuperInterfaces();
            this.refSuperInterfaces = new Reference[superInterfaces.size()];
            for (int i = 0; i < this.refSuperInterfaces.length; ++i) {
            	final ClassFile cfSuperinterface = superInterfaces.get(i);
                state.ensureInstance_JAVA_CLASS(calc, cfSuperinterface);
                this.refSuperInterfaces[i] = state.referenceToInstance_JAVA_CLASS(cfSuperinterface);
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
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
    		final Calculator calc = this.ctx.getCalculator();
        	try {
        		final ClassFile cf_ARRAY_JAVA_CLASS = state.getClassHierarchy().loadCreateClass("" + ARRAYOF + REFERENCE + JAVA_CLASS + TYPEEND);
        		final Reference refArray = state.createArray(calc, null, calc.valInt(this.refSuperInterfaces.length), cf_ARRAY_JAVA_CLASS);
        		final Array array = (Array) state.getObject(refArray);
        		for (int i = 0; i < this.refSuperInterfaces.length; ++i) {
        			array.setFast(calc.valInt(i), this.refSuperInterfaces[i]);
        		}
        		state.pushOperand(refArray);
        	} catch (HeapMemoryExhaustedException e) {
                throwNew(state, calc, OUT_OF_MEMORY_ERROR);
                exitFromAlgorithm();
        	} catch (WrongClassNameException | ClassFileNotFoundException | ClassFileIllFormedException | 
        			ClassFileNotAccessibleException | IncompatibleClassFileException | BadClassFileVersionException | 
        			RenameUnsupportedException | FastArrayAccessNotAllowedException e) {
        		//this should never happen
        		failExecution(e);
			}
        };
    }
}
