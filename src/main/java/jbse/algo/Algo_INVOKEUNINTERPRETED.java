package jbse.algo;

import static jbse.algo.Util.continueWithBaseLevelImpl;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.INVOKEDYNAMICINTERFACE_OFFSET;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.UNSUPPORTED_CLASS_VERSION_ERROR;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.parametersNumber;
import static jbse.common.Type.splitReturnValueDescriptor;

import java.util.function.Supplier;

import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.dec.exc.DecisionException;
import jbse.mem.Array;
import jbse.mem.State;
import jbse.mem.State.Phase;
import jbse.mem.exc.CannotAssumeSymbolicObjectException;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;
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
 * {@link Algo_INVOKEMETA_Nonbranching} implementing the effect of a method call that
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
            return parametersNumber(this.data.signature().getDescriptor(), this.isStatic);
        };
    }
    
    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> {
        	if (state.phase() == Phase.PRE_INITIAL) {
        		continueWithBaseLevelImpl(state, this.isInterface, this.isSpecial, this.isStatic);
        	}
            final String returnType = splitReturnValueDescriptor(this.methodSignatureImpl.getDescriptor());
            final Value[] args = this.data.operands();
            
            try {
                if (isPrimitive(returnType)) {
                    this.valToLoad = state.getCalculator().applyFunctionPrimitive(returnType.charAt(0), state.getHistoryPoint(), this.functionName, args);
                } else {
                    this.valToLoad = new ReferenceSymbolicApply(returnType, state.getHistoryPoint(), this.functionName, args);
                }
            } catch (InvalidOperandException | InvalidTypeException e) {  //TODO propagate these exceptions
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
            Outcome o = null; //to keep the compiler happy
            try {
                o = this.ctx.decisionProcedure.resolve_XLOAD_GETX(state, this.valToLoad, result);
            //TODO the next catch blocks should disappear, see comments on removing exceptions in jbse.dec.DecisionProcedureAlgorithms.doResolveReference
            } catch (ClassFileNotFoundException e) {
                //TODO this exception should wrap a ClassNotFoundException
                throwNew(state, NO_CLASS_DEFINITION_FOUND_ERROR);
                exitFromAlgorithm();
            } catch (BadClassFileVersionException e) {
                throwNew(state, UNSUPPORTED_CLASS_VERSION_ERROR);
                exitFromAlgorithm();
            } catch (WrongClassNameException e) {
                throwNew(state, NO_CLASS_DEFINITION_FOUND_ERROR); //without wrapping a ClassNotFoundException
                exitFromAlgorithm();
            } catch (IncompatibleClassFileException e) {
                throwNew(state, INCOMPATIBLE_CLASS_CHANGE_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileNotAccessibleException e) {
                throwNew(state, ILLEGAL_ACCESS_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileIllFormedException e) {
                throwVerifyError(state);
                exitFromAlgorithm();
            }
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
    throws ContradictionException, InvalidTypeException, InvalidInputException, InterruptException, 
    SymbolicValueNotAllowedException, ClasspathException, FrozenStateException {
        final ReferenceSymbolic referenceToExpand = drc.getValueToLoad();
        final ClassFile classFileOfTargetObject = drc.getClassFileOfTargetObject();
        try {
            state.assumeExpands(referenceToExpand, classFileOfTargetObject);
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (CannotAssumeSymbolicObjectException e) {
            throw new SymbolicValueNotAllowedException(e);
        }
        //in the case the expansion object is an array, we assume it 
        //to have nonnegative length
        if (classFileOfTargetObject.isArray()) {
            try {
                final Array targetObject = (Array) state.getObject(referenceToExpand);
                final Primitive lengthPositive = targetObject.getLength().ge(state.getCalculator().valInt(0));
                state.assume(this.ctx.decisionProcedure.simplify(lengthPositive));
            } catch (InvalidOperandException | DecisionException e) { //TODO propagate these exception (...and replace DecisionException with something better)
                //this should never happen
                failExecution(e);
            }
        }
    }

    protected final void refineRefNull(State state, DecisionAlternative_XYLOAD_GETX_Null altNull)
    throws ContradictionException, FrozenStateException {
        final ReferenceSymbolic referenceToResolve = altNull.getValueToLoad();
        state.assumeNull(referenceToResolve);
    }
    
    @Override
    protected StrategyRefine_XLOAD_GETX refiner() {
        return new StrategyRefine_XLOAD_GETX() {
            @Override
            public void refineRefExpands(State s, DecisionAlternative_XLOAD_GETX_Expands drc)
            throws ContradictionException, InvalidTypeException, SymbolicValueNotAllowedException, 
            InterruptException, ClasspathException, InvalidInputException {
                Algo_INVOKEUNINTERPRETED.this.refineRefExpands(s, drc);
            }

            @Override
            public void refineRefAliases(State s, DecisionAlternative_XLOAD_GETX_Aliases dro)
            throws ContradictionException {
                //this should never happen
                failExecution("Unexpected aliases resolution of uninterpreted function returning a reference.");
            }

            @Override
            public void refineRefNull(State s, DecisionAlternative_XLOAD_GETX_Null drn) 
            throws ContradictionException, FrozenStateException {
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