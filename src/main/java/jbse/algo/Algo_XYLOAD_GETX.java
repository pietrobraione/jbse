package jbse.algo;

import static jbse.algo.UtilClassInitialization.ensureClassInitialized;
import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.algo.Util.findClassFile;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.common.Type.INT;
import static jbse.common.Type.className;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.isPrimitiveOpStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import jbse.algo.exc.MissingTriggerParameterException;
import jbse.algo.exc.NotYetImplementedException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.ClassFile;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Array;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.CannotAssumeSymbolicObjectException;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Aliases;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Null;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Expands;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Loads;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * Abstract {@link Algorithm} for *load*, *aload, get* bytecodes 
 * ([a/d/f/i/l]load[_0/1/2/3], [a/b/c/d/f/i/l/s]aload,  get[field/static]).
 * It manages refinement in the case the {@link Value} 
 * is a {@link ReferenceSymbolic} ("lazy initialization"), and/or it comes 
 * from an {@link Array}.
 * 
 * @author Pietro Braione
 */
abstract class Algo_XYLOAD_GETX<
D extends BytecodeData, 
R extends DecisionAlternative, 
DE extends StrategyDecide<R>, 
RE extends StrategyRefine<R>, 
UP extends StrategyUpdate<R>> extends Algorithm<D, R, DE, RE, UP> {
    //set by subclasses (decider method)
    protected boolean someReferencePartiallyResolved;
    protected ArrayList<ReferenceSymbolic> partiallyResolvedReferences;

    @Override
    protected final void cleanup() {
        this.someReferencePartiallyResolved = false;
        this.partiallyResolvedReferences = new ArrayList<>();
        super.cleanup();
    }
    
    //TODO unify with Algo_INVOKEMETA_Metacircular
    protected final void refineRefExpands(State state, DecisionAlternative_XYLOAD_GETX_Expands drc) 
    throws ContradictionException, InvalidTypeException, InvalidInputException, InterruptException, 
    SymbolicValueNotAllowedException, ClasspathException {
    	final Calculator calc = this.ctx.getCalculator();
        final ReferenceSymbolic referenceToExpand = drc.getValueToLoad();
        final String classNameOfReferenceToExpand = className(referenceToExpand.getStaticType());
        final ClassFile classFileOfReferenceToExpand = findClassFile(state, classNameOfReferenceToExpand);                        
        final ClassFile classFileOfTargetObject = drc.getClassFileOfTargetObject();
        try {
            ensureClassInitialized(state, this.ctx, classFileOfReferenceToExpand, classFileOfTargetObject);
            state.assumeExpands(calc, referenceToExpand, classFileOfTargetObject);
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, calc, OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (CannotAssumeSymbolicObjectException e) {
            throw new SymbolicValueNotAllowedException(e);
        } catch (DecisionException e) {
            //this should never happen, the decision was already checked
        	failExecution(e);
        }
        
        //in the case the expansion object is an array, we assume it 
        //to have nonnegative length
        if (classFileOfTargetObject.isArray()) {
            try {
                final Array targetObject = (Array) state.getObject(referenceToExpand);
                final Primitive lengthPositive = calc.push(targetObject.getLength()).ge(calc.valInt(0)).pop();
                state.assume(calc.simplify(this.ctx.decisionProcedure.simplify(lengthPositive)));
            } catch (InvalidOperandException | DecisionException e) { //TODO propagate exceptions (...and replace DecisionException with a better exception)
                //this should never happen
                failExecution(e);
            }
        }
    }

    protected final void refineRefAliases(State state, DecisionAlternative_XYLOAD_GETX_Aliases altAliases)
    throws ContradictionException, InvalidInputException, ClasspathException, InterruptException {
        final ReferenceSymbolic referenceToResolve = altAliases.getValueToLoad();
        final String classNameOfReferenceToResolve = className(referenceToResolve.getStaticType());
        final ClassFile classFileOfReferenceToResolve = findClassFile(state, classNameOfReferenceToResolve);
        final Objekt aliasObject = state.getObject(new ReferenceConcrete(altAliases.getObjectPosition()));
        try {
            ensureClassInitialized(state, this.ctx, classFileOfReferenceToResolve);
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, this.ctx.getCalculator(), OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (DecisionException e) {
            //this should never happen, the decision was already checked
            throw new UnexpectedInternalException(e);
        }
        state.assumeAliases(referenceToResolve, aliasObject.getOrigin());
    }

    protected final void refineRefNull(State state, DecisionAlternative_XYLOAD_GETX_Null altNull)
    throws ContradictionException, InvalidInputException {
        final ReferenceSymbolic referenceToResolve = altNull.getValueToLoad();
        state.assumeNull(referenceToResolve);
    }

    protected final void update(State state, DecisionAlternative_XYLOAD_GETX_Loads altLoads) 
    throws DecisionException, InterruptException, MissingTriggerParameterException, 
    ClasspathException, NotYetImplementedException, ThreadStackEmptyException, 
    InvalidOperandException, InvalidInputException {
        //possibly materializes the value
        final Value val = altLoads.getValueToLoad();
        final Value valMaterialized = possiblyMaterialize(state, val);
        final char valMaterializedType = valMaterialized.getType();

        //pushes the value
        try {
            final Value valToPush;
            if (isPrimitive(valMaterializedType) && !isPrimitiveOpStack(valMaterializedType)) {
                valToPush = this.ctx.getCalculator().push((Primitive) valMaterialized).widen(INT).pop();
            } else {
                valToPush = valMaterialized;
            }
            state.pushOperand(valToPush);
        } catch (ClassCastException | InvalidTypeException e) { //TODO propagate exceptions
            //this should not happen
            failExecution(e);
        }

        //manages triggers
        try {
            final boolean someTriggerFrameLoaded = 
                this.ctx.triggerManager.loadTriggerFrames(state, this.ctx.getCalculator(), altLoads, this.programCounterUpdate.get());
            if (someTriggerFrameLoaded) {
                exitFromAlgorithm();
            }
        } catch (InvalidProgramCounterException e) { //TODO propagate exception?
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
		}
    }
    
    @Override
    protected final Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    /** 
     * Materializes an immaterial {@link Value}.
     * 
     * @param s a {@link State}
     * @param val the {@link Value} to be materialized.
     * @return a materialized {@link Value}.
     * @throws DecisionException
     * @throws InterruptException 
     * @throws ClasspathException 
     * @throws FrozenStateException 
     */
    protected abstract Value possiblyMaterialize(State s, Value val) 
    throws DecisionException, InterruptException, ClasspathException, FrozenStateException;

    @Override
    public final boolean someReferencePartiallyResolved() { 
        return this.someReferencePartiallyResolved; 
    }

    @Override
    public final List<ReferenceSymbolic> partiallyResolvedReferences() { 
        return this.partiallyResolvedReferences; 
    }
}
