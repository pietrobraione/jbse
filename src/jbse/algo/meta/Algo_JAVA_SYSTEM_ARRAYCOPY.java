package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.ARRAY_STORE_EXCEPTION;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;

import java.util.function.Supplier;

import jbse.algo.StrategyUpdate;
import jbse.mem.Array;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Primitive;
import jbse.val.Reference;

public final class Algo_JAVA_SYSTEM_ARRAYCOPY extends Algo_INVOKEMETA {
    public Algo_JAVA_SYSTEM_ARRAYCOPY() {
        super(false);
    }
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 5;
    }
    
    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            Reference src = null, dest = null;
            Primitive srcPos = null, destPos = null, length = null;
            try {
                src = (Reference) this.data.operand(0);
                srcPos = (Primitive) this.data.operand(1);
                dest = (Reference) this.data.operand(2);
                destPos = (Primitive) this.data.operand(3);
                length = (Primitive) this.data.operand(4);
            } catch (ClassCastException e) {
                throwVerifyError(state);
                exitFromAlgorithm();
            }
            
            if (state.isNull(src) || state.isNull(dest)) {
                throwNew(state, NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }

            Array srcArray = null, destArray = null;
            try {
                srcArray = (Array) state.getObject(src);
                destArray = (Array) state.getObject(dest);
            } catch (ClassCastException e) {
                throwNew(state, ARRAY_STORE_EXCEPTION);
                exitFromAlgorithm();
            }
            
            //TODO
            
        };
	}
}
