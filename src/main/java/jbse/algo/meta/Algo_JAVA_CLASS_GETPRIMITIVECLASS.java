package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.bc.Signatures.CLASS_NOT_FOUND_EXCEPTION;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.State;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Reference;

/**
 * Meta-level implementation of {@link java.lang.Class#getPrimitiveClass(String)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASS_GETPRIMITIVECLASS extends Algo_INVOKEMETA_Nonbranching {
    private Reference classRef; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }
    
    @Override
    protected void cookMore(State state) 
    throws ClasspathException, CannotManageStateException, InterruptException, InvalidInputException {
    	final Calculator calc = this.ctx.getCalculator();
        try {           
            //gets the canonical name of the primitive type and converts it to a string
            final Reference typeNameRef = (Reference) this.data.operand(0);
            final String typeName = valueString(state, typeNameRef);
            if (typeName == null) {
                throw new SymbolicValueNotAllowedException("the String parameter to java.lang.Class.getPrimitiveClass method cannot be a symbolic String");
            }

            //gets the instance of the class
            state.ensureInstance_JAVA_CLASS_primitiveOrVoid(calc, typeName);
            this.classRef = state.referenceToInstance_JAVA_CLASS_primitiveOrVoid(typeName);
        } catch (ClassFileNotFoundException e) {
            throwNew(state, calc, CLASS_NOT_FOUND_EXCEPTION);  //this is how Hotspot behaves
            exitFromAlgorithm();
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
            state.pushOperand(classRef);
        };
    }
}
