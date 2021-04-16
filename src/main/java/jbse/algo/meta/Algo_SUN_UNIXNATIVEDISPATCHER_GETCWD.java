package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.SUN_UNIXNATIVEDISPATCHER;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.binaryClassName;
import static jbse.common.Type.BYTE;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.ReferenceConcrete;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link sun.nio.fs.UnixNativeDispatcher#getcwd()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_SUN_UNIXNATIVEDISPATCHER_GETCWD extends Algo_INVOKEMETA_Nonbranching {
	private ReferenceConcrete cwd; //set by cookMore
	
	@Override
	protected Supplier<Integer> numOperands() {
		return () -> 0;
	}
	
	@Override
	protected void cookMore(State state) throws InvalidInputException, InvalidTypeException, 
	ClasspathException, InterruptException, RenameUnsupportedException {
        final Calculator calc = this.ctx.getCalculator();
		try {
			final Class<?> class_SUN_UNIXNATIVEDISPATCHER = Class.forName(binaryClassName(SUN_UNIXNATIVEDISPATCHER));
			final Method getcwdMethod = class_SUN_UNIXNATIVEDISPATCHER.getDeclaredMethod("getcwd");
			getcwdMethod.setAccessible(true);
			final byte[] cwd = (byte []) getcwdMethod.invoke(null);
            final ClassFile cf_arrayOfBYTE = state.getClassHierarchy().loadCreateClass("" + ARRAYOF + BYTE);
			this.cwd = state.createArray(this.ctx.getCalculator(), null, calc.valInt(cwd.length), cf_arrayOfBYTE);
			final Array cwdArray = (Array) state.getObject(this.cwd);
			for (int i = 0; i < cwd.length; ++i) {
				cwdArray.setFast(calc.valInt(i), calc.valByte(cwd[i]));
			}
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, calc, OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
		} catch (ClassNotFoundException e) {
			//this may happen with a badly misconfigured JBSE, which 
			//is using a UNIX JRE but is running on a non-UNIX platform, 
			//breaking the metacircularity hypothesis
            failExecution(e);
		} catch (NoSuchMethodException e) {
			//this may happen if the version of the JRE used by JBSE
			//is very different from the one we are currently assuming,
			//i.e., Java 8. AFAIK all Java 8 JREs should have this field
            failExecution(e);
		} catch (SecurityException | InvocationTargetException | IllegalAccessException | WrongClassNameException | 
				 ClassFileNotFoundException | ClassFileIllFormedException | ClassFileNotAccessibleException | 
				 IncompatibleClassFileException | BadClassFileVersionException | FastArrayAccessNotAllowedException e) {
			//this should never happen
            //TODO I'm not quite sure that SecurityException can never be raised
            failExecution(e);
		}
	}
	
	@Override
	protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
        	state.pushOperand(this.cwd);
        };
	}
}
