package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.ensureClassCreatedAndInitialized;
import static jbse.algo.Util.ensureInstance_JAVA_CLASS;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.bc.Signatures.CLASS_NOT_FOUND_EXCEPTION;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.dec.exc.InvalidInputException;
import jbse.mem.State;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.lang.Class#forName0(String, boolean, ClassLoader, Class)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASS_FORNAME0 extends Algo_INVOKEMETA_Nonbranching {
    private String className; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 4;
    }

    @Override
    protected void cookMore(State state) 
    throws ThreadStackEmptyException, DecisionException, 
    ClasspathException, SymbolicValueNotAllowedException, 
    InterruptException {
        try {
            //gets the "this" object and the name of its class
            final Reference classNameRef = (Reference) this.data.operand(0);
            if (state.isNull(classNameRef)) {
                throwNew(state, NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }
            this.className = valueString(state, (Reference) this.data.operand(0)).replace('.', '/');
            if (className == null) {
                throw new SymbolicValueNotAllowedException("The className parameter to java.lang.Class.forName0 cannot be a symbolic String");
            }
            if (!(this.data.operand(1) instanceof Primitive)) {
                throwVerifyError(state);
                exitFromAlgorithm();
            } else if (!(this.data.operand(1) instanceof Simplex)) {
                throw new SymbolicValueNotAllowedException("The toInit parameter to java.lang.Class.forName0 cannot be a symbolic boolean");
            }
            final boolean toInit = (((Integer) ((Simplex) this.data.operand(1)).getActualValue()).intValue() > 0);
            ensureInstance_JAVA_CLASS(state, this.className, this.className, this.ctx);
            if (toInit) {
                ensureClassCreatedAndInitialized(state, this.className, this.ctx);
            }
        } catch (ClassFileNotFoundException e) {
            throwNew(state, CLASS_NOT_FOUND_EXCEPTION);
            exitFromAlgorithm();
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (ClassFileNotAccessibleException | BadClassFileException | InvalidInputException e) {
            //this should never happen
            failExecution(e);
        }
    }

    @Override
    protected void update(State state) throws ThreadStackEmptyException {
        //gets the instance of the class of the "this" object
        final Reference classRef = state.referenceToInstance_JAVA_CLASS(this.className);
        state.pushOperand(classRef);
    }
}
