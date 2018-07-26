package jbse.algo;

import static jbse.algo.Util.failExecution;
import static jbse.bc.Offsets.INVOKEDYNAMICINTERFACE_OFFSET;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;
import static jbse.common.Type.splitParametersDescriptors;
import static jbse.common.Type.isArray;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.splitReturnValueDescriptor;

import java.util.function.Supplier;

import jbse.bc.Signature;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.mem.Array;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.tree.DecisionAlternative_XLOAD_GETX;
import jbse.tree.DecisionAlternative_XLOAD_GETX_Aliases;
import jbse.tree.DecisionAlternative_XLOAD_GETX_Expands;
import jbse.tree.DecisionAlternative_XLOAD_GETX_Null;
import jbse.tree.DecisionAlternative_XLOAD_GETX_Resolved;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Expands;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Null;
import jbse.val.Primitive;
import jbse.val.ReferenceSymbolic;
import jbse.val.ReferenceSymbolicApply;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * {@link Algorithm} implementing the effect of a method call that
 * produces as result a symbolic uninterpreted function application
 * on its arguments. Works only for methods that accept as parameters
 * (except possibly the {@code this} parameter) and produce as return
 * value only primitive values.
 * 
 * @author Pietro Braione
 */
//TODO merge with Algo_INVOKEX_Abstract and subclasses and with Algo_XYLOAD_GETX and subclasses
public final class Algo_INVOKEUNINTERPRETED extends Algo_INVOKEMETA<
DecisionAlternative_XLOAD_GETX,
StrategyDecide<DecisionAlternative_XLOAD_GETX>,
StrategyRefine<DecisionAlternative_XLOAD_GETX>,
StrategyUpdate<DecisionAlternative_XLOAD_GETX>> {

    private final Signature methodSignatureImpl; //set by constructor
    private final String functionName; //set by constructor
    
    private Value valToLoad; //set by cooker
    private boolean someRefNotExpanded; //set by decider
    private String nonExpandedRefTypes; //set by decider
    private String nonExpandedRefOrigins; //set by decider

    public Algo_INVOKEUNINTERPRETED(Signature methodSignatureImpl, String functionName) {
        this.methodSignatureImpl = methodSignatureImpl;
        this.functionName = functionName;
    }

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> {
            final String[] paramsDescriptors = splitParametersDescriptors(this.data.signature().getDescriptor());
            return (this.isStatic ? paramsDescriptors.length : paramsDescriptors.length + 1);
        };
    }
    
    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> {
            final String returnType = splitReturnValueDescriptor(this.methodSignatureImpl.getDescriptor());

            //pops the args
            final Value[] args = this.data.operands();
            
            try {
                if (isPrimitive(returnType)) {
                    this.valToLoad = state.getCalculator().applyFunctionPrimitive(returnType.charAt(0), state.getHistoryPoint(), this.functionName, args);
                } else {
                    this.valToLoad = new ReferenceSymbolicApply(returnType, state.getHistoryPoint(), this.functionName, args);
                }
            } catch (InvalidOperandException | InvalidTypeException e) {
                //this should never happen
                failExecution(e);
            }
        };
    }
    
    @Override
    protected Class<DecisionAlternative_XLOAD_GETX> classDecisionAlternative() {
        return DecisionAlternative_XLOAD_GETX.class;
    }
    
    @Override
    protected StrategyDecide<DecisionAlternative_XLOAD_GETX> decider() {
        return (state, result) -> {
            final Outcome o = this.ctx.decisionProcedure.resolve_XLOAD_GETX(state, this.valToLoad, result);
            this.someRefNotExpanded = o.noReferenceExpansion();
            if (this.someRefNotExpanded) {
                try {
                    final ReferenceSymbolic refToLoad = (ReferenceSymbolic) this.valToLoad;
                    this.nonExpandedRefTypes = refToLoad.getStaticType();
                    this.nonExpandedRefOrigins = refToLoad.asOriginString();
                } catch (ClassCastException e) {
                    throw new UnexpectedInternalException(e);
                }
            }
            return o;
        };
    }


    protected final void refineRefExpands(State state, DecisionAlternative_XYLOAD_GETX_Expands drc) 
    throws ContradictionException, InvalidTypeException {
        final ReferenceSymbolic referenceToExpand = drc.getValueToLoad();
        final String classNameOfTargetObject = drc.getClassNameOfTargetObject();
        state.assumeExpands(referenceToExpand, classNameOfTargetObject);
        //in the case the expansion object is an array, we assume it 
        //to have nonnegative length
        if (isArray(classNameOfTargetObject)) {
            try {
                final Array targetObject = (Array) state.getObject(referenceToExpand);
                final Primitive lengthPositive = targetObject.getLength().ge(state.getCalculator().valInt(0));
                state.assume(this.ctx.decisionProcedure.simplify(lengthPositive));
            } catch (InvalidOperandException | InvalidTypeException e) {
                //this should never happen
                failExecution(e);
            }
        }
    }

    protected final void refineRefNull(State state, DecisionAlternative_XYLOAD_GETX_Null altNull)
    throws ContradictionException {
        final ReferenceSymbolic referenceToResolve = altNull.getValueToLoad();
        state.assumeNull(referenceToResolve);
    }
    
    @Override
    protected StrategyRefine_XLOAD_GETX refiner() {
        return new StrategyRefine_XLOAD_GETX() {
            @Override
            public void refineRefExpands(State s, DecisionAlternative_XLOAD_GETX_Expands drc)
            throws ContradictionException, InvalidTypeException {
                Algo_INVOKEUNINTERPRETED.this.refineRefExpands(s, drc);
            }

            @Override
            public void refineRefAliases(State s, DecisionAlternative_XLOAD_GETX_Aliases dro)
            throws ContradictionException {
                //this should never happen
                failExecution("Unexpected aliases resolution of uninterpreted function returning a reference.");
            }

            @Override
            public void refineRefNull(State s, DecisionAlternative_XLOAD_GETX_Null drn) throws ContradictionException {
                Algo_INVOKEUNINTERPRETED.this.refineRefNull(s, drn);
            }

            @Override
            public void refineResolved(State s, DecisionAlternative_XLOAD_GETX_Resolved drr) {
                //nothing to do, the value is concrete or has been already refined                
            }
        };
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_XLOAD_GETX> updater() {
        return (state, alt) -> {
            final Value valToPush = alt.getValueToLoad();
            state.pushOperand(valToPush);
        };
    }
    
    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }
    
    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> (this.isInterface ? INVOKEDYNAMICINTERFACE_OFFSET : 
                                         INVOKESPECIALSTATICVIRTUAL_OFFSET);
    }

    @Override
    public final boolean someReferenceNotExpanded() { 
        return this.someRefNotExpanded; 
    }

    @Override
    public final String nonExpandedReferencesTypes() { 
        return this.nonExpandedRefTypes; 
    }

    @Override
    public final String nonExpandedReferencesOrigins() { 
        return this.nonExpandedRefOrigins; 
    }
}