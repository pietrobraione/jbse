package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;
import static jbse.bc.Signatures.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION;
import static jbse.bc.Signatures.ARRAY_STORE_EXCEPTION;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;
import static jbse.common.Type.getArrayMemberType;
import static jbse.common.Type.isPrimitive;

import java.util.Iterator;
import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA;
import jbse.algo.BytecodeCooker;
import jbse.algo.StrategyDecide;
import jbse.algo.StrategyRefine;
import jbse.algo.StrategyUpdate;
import jbse.bc.exc.BadClassFileException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.mem.Array;
import jbse.tree.DecisionAlternative_XASTORE;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

public final class Algo_JAVA_SYSTEM_ARRAYCOPY extends Algo_INVOKEMETA<
DecisionAlternative_XASTORE,
StrategyDecide<DecisionAlternative_XASTORE>, 
StrategyRefine<DecisionAlternative_XASTORE>, 
StrategyUpdate<DecisionAlternative_XASTORE>> {
    Reference src = null, dest = null; //produced by the cooker
    Primitive srcPos = null, destPos = null, length = null, inRange = null; //produced by the cooker

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 5;
    }
    
    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> {
            try {
                this.src = (Reference) this.data.operand(0);
                this.srcPos = (Primitive) this.data.operand(1);
                this.dest = (Reference) this.data.operand(2);
                this.destPos = (Primitive) this.data.operand(3);
                this.length = (Primitive) this.data.operand(4);
            } catch (ClassCastException e) {
                throwVerifyError(state);
                exitFromAlgorithm();
            }

            if (state.isNull(this.src) || state.isNull(this.dest)) {
                throwNew(state, NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }

            Array srcArray = null, destArray = null;
            try {
                srcArray = (Array) state.getObject(this.src);
                destArray = (Array) state.getObject(this.dest);
            } catch (ClassCastException e) {
                throwNew(state, ARRAY_STORE_EXCEPTION);
                exitFromAlgorithm();
            }
            
            final String srcTypeComponent = getArrayMemberType(srcArray.getType());
            final String destTypeComponent = getArrayMemberType(destArray.getType());
            //this is actually stronger than what required by the arraylength 
            //specification (that allows dynamic assignment compatibility), 
            //but implementing the latter would be too complex
            if (isPrimitive(srcTypeComponent) && 
                isPrimitive(destTypeComponent) &&
                !srcTypeComponent.equals(destTypeComponent)) {
                throwNew(state, ARRAY_STORE_EXCEPTION);
                exitFromAlgorithm();
            } else if (isPrimitive(srcTypeComponent) != isPrimitive(destTypeComponent)) {
                throwNew(state, ARRAY_STORE_EXCEPTION);
                exitFromAlgorithm();
            } else try {
                if (state.getClassHierarchy().isAssignmentCompatible(srcTypeComponent, destTypeComponent)) {
                    throwNew(state, ARRAY_STORE_EXCEPTION);
                    exitFromAlgorithm();
                }
            } catch (BadClassFileException exc) {
                throwVerifyError(state);
                exitFromAlgorithm();
            }
            
            final Primitive zero = this.ctx.calc.valInt(0);
            try {
                this.inRange = this.srcPos.ge(zero)
                               .and(this.destPos.ge(zero))
                               .and(this.length.ge(zero))
                               .and(this.srcPos.add(this.length).lt(srcArray.getLength()))
                               .and(this.destPos.add(this.length).lt(destArray.getLength()));
            } catch (InvalidOperandException | InvalidTypeException e) {
                throwVerifyError(state);
                exitFromAlgorithm();
            }
        };
    } 
    
    @Override
    protected Class<DecisionAlternative_XASTORE> classDecisionAlternative() {
        return DecisionAlternative_XASTORE.class;
    }
    
    @Override
    protected StrategyDecide<DecisionAlternative_XASTORE> decider() {
        return (state, result) -> {
            final Outcome o = this.ctx.decisionProcedure.decide_XASTORE(state.getClassHierarchy(), this.inRange, result);
            return o;
        };
    }
    
    @Override
    protected StrategyRefine<DecisionAlternative_XASTORE> refiner() {
        return (state, alt) -> {
            state.assume(this.ctx.decisionProcedure.simplify(alt.isInRange() ? this.inRange : this.inRange.neg()));
        };
    }
    
    @Override
    protected StrategyUpdate<DecisionAlternative_XASTORE> updater() {
        return (state, alt) -> {
            if (alt.isInRange()) {
                Array srcArray = null, destArray = null;
                try {
                    srcArray = (Array) state.getObject(this.src);
                    destArray = (Array) state.getObject(this.dest);
                } catch (ClassCastException e) {
                    //this should not happen now
                    throw new UnexpectedInternalException(e);
                }
                final Iterator<Array.AccessOutcomeIn> entries = destArray.arraycopy(srcArray, this.srcPos, this.destPos, this.length);
                this.ctx.decisionProcedure.completeArraycopy(state.getClassHierarchy(), entries, this.srcPos, this.destPos, this.length);
            } else {
                throwNew(state, ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION);
                exitFromAlgorithm();
            }
        };
    }
    
    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }
    
    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> INVOKESPECIALSTATICVIRTUAL_OFFSET;
    }
}
