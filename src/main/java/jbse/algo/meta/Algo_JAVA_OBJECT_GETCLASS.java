package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.ensureInstance_JAVA_CLASS;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;

/**
 * Meta-level implementation of {@link java.lang.Object#getClass()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_OBJECT_GETCLASS extends Algo_INVOKEMETA_Nonbranching {
    String className; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }
    
    @Override
    protected void cookMore(State state) 
    throws ThreadStackEmptyException, DecisionException, 
    ClasspathException, SymbolicValueNotAllowedException, 
    InterruptException {
        try {
            //gets the "this" object and the name of its class
            final Reference thisRef = (Reference) this.data.operand(0);
            if (state.isNull(thisRef)) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Object.getClass method is null.");
            }
            final Objekt thisObj = state.getObject(thisRef);
            if (thisObj == null) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Object.getClass method is symbolic and unresolved.");
            }
            this.className = thisObj.getType();
            ensureInstance_JAVA_CLASS(state, this.className, this.className, this.ctx);
        } catch (ClassFileNotAccessibleException e) {
            throwNew(state, ILLEGAL_ACCESS_ERROR);
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (BadClassFileException e) {
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
