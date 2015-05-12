package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.ensureInstance_JAVA_CLASS;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;

import java.util.function.Supplier;

import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.mem.Objekt;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;

public final class Algo_JAVA_OBJECT_GETCLASS extends Algo_INVOKEMETA {
    public Algo_JAVA_OBJECT_GETCLASS() {
        super(false);
    }
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }
    
    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            try {
                //gets the "this" object and the name of its class
                final Reference thisRef = (Reference) this.data.operand(0);
                if (state.isNull(thisRef)) {
                    throwNew(state, NULL_POINTER_EXCEPTION);
                    exitFromAlgorithm();
                }
                final Objekt thisObj = state.getObject(thisRef);
                if (thisObj == null) {
                    throw new SymbolicValueNotAllowedException("The 'this' parameter to java.lang.Object.getClass method is symbolic.");
                }
                final String className = thisObj.getType();

                //gets the instance of the class of the "this" object
                ensureInstance_JAVA_CLASS(state, className, ctx.decisionProcedure);
                final Reference classRef = state.referenceToInstance_JAVA_CLASS(className);
                state.pushOperand(classRef);
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
        };
    }
}
