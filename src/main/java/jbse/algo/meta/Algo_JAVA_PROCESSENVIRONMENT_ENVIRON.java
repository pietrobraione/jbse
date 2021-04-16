package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.BYTE;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.StrategyUpdate;
import jbse.bc.ClassFile;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.mem.Array;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.ReferenceConcrete;

/**
 * Meta-level implementation of {@link java.lang.ProcessEnvironment#environ()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_PROCESSENVIRONMENT_ENVIRON extends Algo_INVOKEMETA_Nonbranching {
    private byte[][] readBytes; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 0;
    }

    @Override
    protected void cookMore(State state) {
        //invokes metacircularly the environ method
    	try {
    		final Class<?> classProcessEnvironment = Class.forName("java.lang.ProcessEnvironment"); 
    		final Method method = classProcessEnvironment.getDeclaredMethod("environ");
    		method.setAccessible(true);
    		this.readBytes = (byte[][]) method.invoke(null);
    	} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | 
    			IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
    		//this should never happen
    		failExecution(e);
    	}
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
        	final Calculator calc = this.ctx.getCalculator();
            try {
            	final ClassFile cf_ARRAYOF_ARRAYOF_BYTE = state.getClassHierarchy().loadCreateClass("" + ARRAYOF + ARRAYOF + BYTE);
            	final ClassFile cf_ARRAYOF_BYTE = state.getClassHierarchy().loadCreateClass("" + ARRAYOF + BYTE);
            	final ReferenceConcrete referenceArray = state.createArray(calc, null, calc.valInt(this.readBytes.length), cf_ARRAYOF_ARRAYOF_BYTE);
            	state.pushOperand(referenceArray);

            	final Array array = (Array) state.getObject(referenceArray);
            	for (int i = 0; i < this.readBytes.length; ++i) {
            		final ReferenceConcrete referenceArrayComponent = state.createArray(calc, null, calc.valInt(this.readBytes[i].length), cf_ARRAYOF_BYTE);
            		array.setFast(calc.valInt(i), referenceArrayComponent);
                	final Array arrayComponent = (Array) state.getObject(referenceArrayComponent);
                	for (int k = 0; k < this.readBytes[i].length; ++k) {
                		arrayComponent.setFast(calc.valInt(k), calc.valByte(this.readBytes[i][k]));
                	}
            	}
            } catch (HeapMemoryExhaustedException e) {
                throwNew(state, calc, OUT_OF_MEMORY_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileNotFoundException | ClassFileIllFormedException | ClassFileNotAccessibleException | 
            		 IncompatibleClassFileException | BadClassFileVersionException | RenameUnsupportedException |
            		 WrongClassNameException | FastArrayAccessNotAllowedException e) {
                //this should never happen
                failExecution(e);
			}
        };
    }
}
