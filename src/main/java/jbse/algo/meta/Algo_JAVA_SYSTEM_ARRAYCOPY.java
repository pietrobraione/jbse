package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;
import static jbse.bc.Signatures.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION;
import static jbse.bc.Signatures.ARRAY_STORE_EXCEPTION;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;

import java.util.Iterator;
import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA;
import jbse.algo.BytecodeCooker;
import jbse.algo.StrategyDecide;
import jbse.algo.StrategyRefine;
import jbse.algo.StrategyUpdate;
import jbse.bc.ClassFile;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.mem.Array;
import jbse.mem.Objekt;
import jbse.mem.exc.FrozenStateException;
import jbse.tree.DecisionAlternative_XASTORE;
import jbse.val.Calculator;
import jbse.val.Null;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link java.lang.System#arraycopy(Object, int, Object, int, int)}.
 * 
 * @author Pietro Braione
 */
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
        	final Calculator calc = this.ctx.getCalculator();
            try {
                this.src = (Reference) this.data.operand(0);
                this.srcPos = (Primitive) this.data.operand(1);
                this.dest = (Reference) this.data.operand(2);
                this.destPos = (Primitive) this.data.operand(3);
                this.length = (Primitive) this.data.operand(4);
            } catch (ClassCastException e) {
                throwVerifyError(state, calc);
                exitFromAlgorithm();
            }

            if (state.isNull(this.src) || state.isNull(this.dest)) {
                throwNew(state, calc, NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }

            Array srcArray = null, destArray = null;
            try {
                srcArray = (Array) state.getObject(this.src);
                destArray = (Array) state.getObject(this.dest);
            } catch (ClassCastException e) {
                throwNew(state, calc, ARRAY_STORE_EXCEPTION);
                exitFromAlgorithm();
            }

            final ClassFile srcTypeComponent = srcArray.getType().getMemberClass();
            final ClassFile destTypeComponent = destArray.getType().getMemberClass();
            if (srcTypeComponent.isPrimitiveOrVoid() && 
                destTypeComponent.isPrimitiveOrVoid()) {
                if (!srcTypeComponent.equals(destTypeComponent)) {
                    throwNew(state, calc, ARRAY_STORE_EXCEPTION);
                    exitFromAlgorithm();
                }
            } else if (srcTypeComponent.isPrimitiveOrVoid() != destTypeComponent.isPrimitiveOrVoid()) {
                throwNew(state, calc, ARRAY_STORE_EXCEPTION);
                exitFromAlgorithm();
            }

            final Primitive zero = calc.valInt(0);
            try {
                this.inRange = calc.push(this.srcPos).ge(zero)
                               .and(calc.push(this.destPos).ge(zero).pop())
                               .and(calc.push(this.length).ge(zero).pop())
                               .and(calc.push(this.srcPos).add(this.length).le(srcArray.getLength()).pop())
                               .and(calc.push(this.destPos).add(this.length).le(destArray.getLength()).pop())
                               .pop();
            } catch (InvalidOperandException | InvalidTypeException e) {
                throwVerifyError(state, calc);
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
            final Outcome o = this.ctx.decisionProcedure.decide_XASTORE(this.inRange, result);
            return o;
        };
    }

    @Override
    protected StrategyRefine<DecisionAlternative_XASTORE> refiner() {
        return (state, alt) -> {
            state.assume(this.ctx.getCalculator().simplify(this.ctx.decisionProcedure.simplify(alt.isInRange() ? this.inRange : this.ctx.getCalculator().push(this.inRange).not().pop())));
        };
    }

    private static class ExitFromAlgorithmException extends RuntimeException {
        private static final long serialVersionUID = 7040464752195180704L;        
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_XASTORE> updater() {
        return (state, alt) -> {
        	final Calculator calc = this.ctx.getCalculator();
            if (alt.isInRange()) {
                Array srcArray = null, destArray = null;
                try {
                    srcArray = (Array) state.getObject(this.src);
                    destArray = (Array) state.getObject(this.dest);
                    final ClassFile destTypeComponent = destArray.getType().getMemberClass();
                    final ClasspathException[] _eCP = new ClasspathException[1]; //boxes so the next closure can store the exception
                    final FrozenStateException[] _eFS = new FrozenStateException[1]; //boxes so the next closure can store the exception
                    final Iterator<? extends Array.AccessOutcomeIn> entries = 
                        destArray.arraycopy(calc, srcArray, this.srcPos, this.destPos, this.length,  
                                        (Reference ref) -> {
                                            if (ref instanceof Null) {
                                                return;
                                            }
                                            try {
                                                final Objekt srcElement = state.getObject(ref);
                                                if (!state.getClassHierarchy().isAssignmentCompatible(srcElement.getType(), destTypeComponent)) {
                                                    throwNew(state, calc, ARRAY_STORE_EXCEPTION);
                                                    throw new ExitFromAlgorithmException();
                                                }
                                            } catch (ClasspathException exc) {
                                                try {
                                                    throwVerifyError(state, calc);
                                                } catch (ClasspathException e) {
                                                    _eCP[0] = e;
                                                    //then falls through
                                                }
                                                throw new ExitFromAlgorithmException();
                                            } catch (FrozenStateException e) {
                                            	_eFS[0] = e;
											}
                                        });
                    if (_eCP[0] != null) {
                        throw _eCP[0];
                    }
                    if (_eFS[0] != null) {
                        throw _eFS[0];
                    }
                    this.ctx.decisionProcedure.completeArraycopy(entries, this.srcPos, this.destPos, this.length);
                } catch (InvalidTypeException | InvalidInputException | ClassCastException e) {
                    //this should never happen
                    failExecution(e);
                } catch (ExitFromAlgorithmException e) {
                    exitFromAlgorithm();
                }
            } else {
                throwNew(state, calc, ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION);
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
