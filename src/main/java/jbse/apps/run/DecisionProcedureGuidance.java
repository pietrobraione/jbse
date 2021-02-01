package jbse.apps.run;

import static jbse.algo.Util.ensureClassInitialized;
import static jbse.bc.ClassLoaders.CLASSLOADER_APP;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedSet;

import jbse.algo.ExecutionContext;
import jbse.algo.InterruptException;
import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedure;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.dec.exc.DecisionException;
import jbse.jvm.RunnerParameters;
import jbse.mem.Clause;
import jbse.mem.ClauseAssumeExpands;
import jbse.mem.State;
import jbse.mem.SwitchTable;
import jbse.mem.State.Phase;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_XALOAD;
import jbse.tree.DecisionAlternative_XALOAD_Unresolved;
import jbse.tree.DecisionAlternative_XASTORE;
import jbse.tree.DecisionAlternative_XCMPY;
import jbse.tree.DecisionAlternative_IFX;
import jbse.tree.DecisionAlternative_XLOAD_GETX;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Unresolved;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Aliases;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Expands;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Null;
import jbse.tree.DecisionAlternative_XNEWARRAY;
import jbse.tree.DecisionAlternative_XSWITCH;
import jbse.val.Any;
import jbse.val.Calculator;
import jbse.val.Expression;
import jbse.val.PrimitiveSymbolicApply;
import jbse.val.PrimitiveSymbolicAtomic;
import jbse.val.NarrowingConversion;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.PrimitiveVisitor;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;
import jbse.val.Simplex;
import jbse.val.Symbolic;
import jbse.val.Term;
import jbse.val.WideningConversion;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * {@link DecisionProcedureAlgorithms} for guided symbolic execution. 
 * It keeps a private JVM that runs a guiding concrete execution up to the 
 * concrete counterpart of the initial state, and filters all the decisions taken by 
 * the component decision procedure it decorates by evaluating the submitted clauses
 * in the state reached by the private JVM.
 */
public abstract class DecisionProcedureGuidance extends DecisionProcedureAlgorithms {
    protected final JVM jvm;
    private final HashSet<Object> seen = new HashSet<>();
    private ExecutionContext ctx; //TODO remove the dependency from ExecutionContext
    private boolean guiding;    

    /**
     * Builds the {@link DecisionProcedureGuidance}.
     *
     * @param component the component {@link DecisionProcedure} it decorates.
     * @param jvm a (guiding) {@link JVM}.
     * @throws GuidanceException if something fails during creation (and the caller
     *         is to blame).
     * @throws InvalidInputException if {@code component == null}.
     */
    public DecisionProcedureGuidance(DecisionProcedure component, JVM jvm) 
    throws GuidanceException, InvalidInputException {
        super(component);
        goFastAndImprecise(); //disables theorem proving of component until guidance ends
        this.jvm = jvm;
        this.ctx = null;
        this.guiding = true;
    }
    
    public final void setExecutionContext(ExecutionContext ctx) {
    	this.ctx = ctx;
    }
    
    /**
     * Ends guidance decision, and falls back on the 
     * component decision procedure.
     */
    public final void endGuidance() {
        this.guiding = false;
        stopFastAndImprecise();
    }

    @Override
    public final void pushAssumption(Clause c) 
    throws InvalidInputException, DecisionException {
        super.pushAssumption(c);
        if (this.guiding) {
            if (c instanceof ClauseAssumeExpands) {
                final ClauseAssumeExpands cExp = (ClauseAssumeExpands) c;
                markAsSeen(cExp.getReference());
            }
        }
    }
    
    @Override
    public void clearAssumptions() throws DecisionException {
        super.clearAssumptions();
        if (this.guiding) {
            clearSeen();
        }
    }
    
    @Override
    public final void addAssumptions(Iterable<Clause> assumptionsToAdd) 
    throws InvalidInputException, DecisionException {
        super.addAssumptions(assumptionsToAdd);
        if (this.guiding) {
            for (Clause c : assumptionsToAdd) {
                if (c instanceof ClauseAssumeExpands) {
                    final ClauseAssumeExpands cExp = (ClauseAssumeExpands) c;
                    markAsSeen(cExp.getReference());
                }
            }
        }
    }
    
    @Override
    public final void addAssumptions(Clause... assumptionsToAdd) 
    throws InvalidInputException, DecisionException {
        super.addAssumptions(assumptionsToAdd);
        if (this.guiding) {
            for (Clause c : assumptionsToAdd) {
                if (c instanceof ClauseAssumeExpands) {
                    final ClauseAssumeExpands cExp = (ClauseAssumeExpands) c;
                    markAsSeen(cExp.getReference());
                }
            }
        }
    }
    
    @Override
    public final void setAssumptions(Collection<Clause> newAssumptions) 
    throws InvalidInputException, DecisionException {
        super.setAssumptions(newAssumptions);
        if (this.guiding) {
            clearSeen();
            for (Clause c : newAssumptions) {
                if (c instanceof ClauseAssumeExpands) {
                    final ClauseAssumeExpands cExp = (ClauseAssumeExpands) c;
                    markAsSeen(cExp.getReference());
                }
            }
        }
    }

    @Override
    protected final Outcome decide_IFX_Nonconcrete(Primitive condition, SortedSet<DecisionAlternative_IFX> result) 
    throws DecisionException {
        final Outcome retVal = super.decide_IFX_Nonconcrete(condition, result);
        if (this.guiding) {
            final Iterator<DecisionAlternative_IFX> it = result.iterator();
            while (it.hasNext()) {
                final DecisionAlternative_IFX da = it.next();
                final Primitive valueInConcreteState = this.jvm.eval_IFX(da, condition);
                if (valueInConcreteState != null && valueInConcreteState.surelyFalse()) {
                    it.remove();
                }
            }
        }
        return retVal;
    }

    @Override
    protected final Outcome decide_XCMPY_Nonconcrete(Primitive val1, Primitive val2, SortedSet<DecisionAlternative_XCMPY> result)
    throws DecisionException {
        final Outcome retVal = super.decide_XCMPY_Nonconcrete(val1, val2, result);
        if (this.guiding) {
            final Iterator<DecisionAlternative_XCMPY> it = result.iterator();
            while (it.hasNext()) {
                final DecisionAlternative_XCMPY da = it.next();
                final Primitive valueInConcreteState = this.jvm.eval_XCMPY(da, val1, val2);
                if (valueInConcreteState != null && valueInConcreteState.surelyFalse()) {
                    it.remove();
                }
            }
        }
        return retVal;
    }

    @Override
    protected final Outcome decide_XSWITCH_Nonconcrete(Primitive selector, SwitchTable tab, SortedSet<DecisionAlternative_XSWITCH> result)
    throws DecisionException {
        final Outcome retVal = super.decide_XSWITCH_Nonconcrete(selector, tab, result);
        if (this.guiding) {
            final Iterator<DecisionAlternative_XSWITCH> it = result.iterator();
            while (it.hasNext()) {
                final DecisionAlternative_XSWITCH da = it.next();
                final Primitive valueInConcreteState = this.jvm.eval_XSWITCH(da, selector, tab);
                if (valueInConcreteState != null && valueInConcreteState.surelyFalse()) {
                    it.remove();
                }
            }
        }
        return retVal;
    }

    @Override
    protected final Outcome decide_XNEWARRAY_Nonconcrete(Primitive countsNonNegative, SortedSet<DecisionAlternative_XNEWARRAY> result)
    throws DecisionException {
        final Outcome retVal = super.decide_XNEWARRAY_Nonconcrete(countsNonNegative, result);
        if (this.guiding) {
            final Iterator<DecisionAlternative_XNEWARRAY> it = result.iterator();
            while (it.hasNext()) {
                final DecisionAlternative_XNEWARRAY da = it.next();
                final Primitive valueInConcreteState = this.jvm.eval_XNEWARRAY(da, countsNonNegative);
                if (valueInConcreteState != null && valueInConcreteState.surelyFalse()) {
                    it.remove();
                }
            }
        }
        return retVal;
    }

    @Override
    protected final Outcome decide_XASTORE_Nonconcrete(Primitive inRange, SortedSet<DecisionAlternative_XASTORE> result)
    throws DecisionException {
        final Outcome retVal = super.decide_XASTORE_Nonconcrete(inRange, result);
        if (this.guiding) {
            final Iterator<DecisionAlternative_XASTORE> it = result.iterator();
            while (it.hasNext()) {
                final DecisionAlternative_XASTORE da = it.next();
                final Primitive valueInConcreteState = this.jvm.eval_XASTORE(da, inRange);
                if (valueInConcreteState != null && valueInConcreteState.surelyFalse()) {
                    it.remove();
                }
            }
        }
        return retVal;
    }

    @Override
    protected final Outcome resolve_XLOAD_GETX_Unresolved(ClassHierarchy hier, ReferenceSymbolic refToLoad, SortedSet<DecisionAlternative_XLOAD_GETX> result)
    throws DecisionException, ClassFileNotFoundException, ClassFileIllFormedException, 
    BadClassFileVersionException, RenameUnsupportedException, WrongClassNameException, 
    IncompatibleClassFileException, ClassFileNotAccessibleException,
    ClasspathException, HeapMemoryExhaustedException, InterruptException, ContradictionException {
    	final State currentState = this.currentStateSupplier.get();
        updateExpansionBackdoor(currentState, refToLoad);
        final Outcome retVal = super.resolve_XLOAD_GETX_Unresolved(hier, refToLoad, result);
        if (this.guiding) {
            final Iterator<DecisionAlternative_XLOAD_GETX> it = result.iterator();
            while (it.hasNext()) {
                final DecisionAlternative_XYLOAD_GETX_Unresolved dar = (DecisionAlternative_XYLOAD_GETX_Unresolved) it.next();
                filter(currentState, refToLoad, dar, it);
            }
        }
        return retVal;
    }

    @Override
    protected final Outcome resolve_XALOAD_ResolvedNonconcrete(ArrayAccessInfo arrayAccessInfo, SortedSet<DecisionAlternative_XALOAD> result)
    throws DecisionException {
        final Outcome retVal = super.resolve_XALOAD_ResolvedNonconcrete(arrayAccessInfo, result);
        if (this.guiding) {
            final Iterator<DecisionAlternative_XALOAD> it = result.iterator();
            while (it.hasNext()) {
                final DecisionAlternative_XALOAD da = it.next();
                final Primitive valueInConcreteState = this.jvm.eval_XALOAD(da);
                if (valueInConcreteState != null && valueInConcreteState.surelyFalse()) {
                    it.remove();
                }
            }
        }
        return retVal;
    }

    @Override
    protected final Outcome resolve_XALOAD_Unresolved(ClassHierarchy hier, ArrayAccessInfo arrayAccessInfo, SortedSet<DecisionAlternative_XALOAD> result)
    throws DecisionException, ClassFileNotFoundException, ClassFileIllFormedException, 
    BadClassFileVersionException, RenameUnsupportedException, WrongClassNameException, 
    IncompatibleClassFileException, ClassFileNotAccessibleException,
    ClasspathException, HeapMemoryExhaustedException, InterruptException, ContradictionException {
    	final State currentState = this.currentStateSupplier.get();
        final ReferenceSymbolic readReference = (ReferenceSymbolic) arrayAccessInfo.readValue;
        updateExpansionBackdoor(currentState, readReference);
        final Outcome retVal = super.resolve_XALOAD_Unresolved(hier, arrayAccessInfo, result);
        if (this.guiding) {
            final Iterator<DecisionAlternative_XALOAD> it = result.iterator();
            while (it.hasNext()) {
                final DecisionAlternative_XALOAD_Unresolved dar = (DecisionAlternative_XALOAD_Unresolved) it.next();
                final Primitive valueInConcreteState = this.jvm.eval_XALOAD(dar);
                if (valueInConcreteState != null && valueInConcreteState.surelyFalse()) {
                    it.remove();
                } else {
                    filter(currentState, readReference, dar, it);
                }
            }
        }
        return retVal;
    }

    private void updateExpansionBackdoor(State state, ReferenceSymbolic refToLoad) 
    throws GuidanceException, ClasspathException, HeapMemoryExhaustedException, 
    InterruptException, ContradictionException {
    	try {
    		final String refType = mostPreciseResolutionClassName(this.currentStateSupplier.get().getClassHierarchy(), refToLoad);
    		final String objType = this.jvm.typeOfObject(refToLoad);
    		if (objType != null && !refType.equals(objType)) {
    			state.getClassHierarchy().addToExpansionBackdoor(refType, objType);
				final ClassFile cf = state.getClassHierarchy().loadCreateClass(CLASSLOADER_APP, objType, true);
    			ensureClassInitialized(state, this.ctx, cf);
    		}
    	} catch (DecisionException | ClassFileNotFoundException | ClassFileIllFormedException | 
    	ClassFileNotAccessibleException | IncompatibleClassFileException | 
    	BadClassFileVersionException | WrongClassNameException e) {
    		throw new GuidanceException(e);
    	} catch (InvalidInputException | RenameUnsupportedException | PleaseLoadClassException e) {
    		//this should never happen
    		throw new UnexpectedInternalException(e);
    	}
    }

    private void filter(State state, ReferenceSymbolic readReference, DecisionAlternative_XYLOAD_GETX_Unresolved dar, Iterator<?> it) 
    throws GuidanceException {
        if (dar instanceof DecisionAlternative_XYLOAD_GETX_Null && !this.jvm.isNull(readReference)) {
            it.remove();
        } else if (dar instanceof DecisionAlternative_XYLOAD_GETX_Aliases) {
            final DecisionAlternative_XYLOAD_GETX_Aliases dara = (DecisionAlternative_XYLOAD_GETX_Aliases) dar;
            final ReferenceSymbolic aliasOrigin;
            try {
                aliasOrigin = state.getObject(new ReferenceConcrete(dara.getObjectPosition())).getOrigin();
            } catch (FrozenStateException e) {
                //this should never happen
                throw new UnexpectedInternalException(e);
            }
            if (!this.jvm.areAlias(readReference, aliasOrigin)) {
                it.remove();
            }
        } else if (dar instanceof DecisionAlternative_XYLOAD_GETX_Expands) {
            final DecisionAlternative_XYLOAD_GETX_Expands dare = (DecisionAlternative_XYLOAD_GETX_Expands) dar;
            if (this.jvm.isNull(readReference) || alreadySeen(readReference) ||
                !dare.getClassFileOfTargetObject().getClassName().equals(this.jvm.typeOfObject(readReference))) {
                it.remove();
            }
        }
    }

    private boolean alreadySeen(ReferenceSymbolic m) throws GuidanceException {
        return this.seen.contains(this.jvm.getValue(m));
    }

    private void markAsSeen(ReferenceSymbolic m) throws GuidanceException {
        this.seen.add(this.jvm.getValue(m));
    }
    
    private void clearSeen() {
        this.seen.clear();
    }
    
    @Override
    public void close() throws DecisionException {
        super.close();
        this.jvm.close();
    }

    public static abstract class JVM {
        protected final Calculator calc;

        /**
         * Constructor. The subclass constructor must launch
         * a JVM and run it until the execution hits the method
         * with signature {@code stopSignature} for {@code numberOfHits}
         * times.
         * 
         * @param calc a {@link Calculator}
         * @param runnerParameters the {@link RunnerParameters} with information
         *        about the classpath and the method to run.
         * @param stopSignature the {@link Signature} of the method where to stop
         *        execution.
         * @param numberOfHits an {@code int}, the number of times {@code stopSignature}
         *        must be hit for the execution to stop.
         * @throws GuidanceException if something goes wrong while the JVM runs.
         */
        public JVM(Calculator calc, RunnerParameters runnerParameters, Signature stopSignature, int numberOfHits) 
        throws GuidanceException {
            if (numberOfHits < 1) {
                throw new GuidanceException("Invalid number of hits " + numberOfHits + ".");
            }
            this.calc = calc;
        }

        /**
         * Returns the class of an object in the reached concrete state.
         * 
         * @param origin the {@link ReferenceSymbolic} to the object.
         * @return a {@link String}, the class of the object referred to
         *         by {@code origin}, or {@code null} if {@code origin}
         *         points to {@code null}.
         * @throws GuidanceException if {@code origin} does not refer to an object.
         */
        public abstract String typeOfObject(ReferenceSymbolic origin) throws GuidanceException;

        /**
         * Returns whether a {@link ReferenceSymbolic} points to {@code null} in the reached 
         * concrete state.
         * 
         * @param origin a {@link ReferenceSymbolic}.
         * @return {@code true} iff {@code origin} points to {@code null}.
         * @throws GuidanceException if {@code origin} does not refer to an object.
         */
        public abstract boolean isNull(ReferenceSymbolic origin) throws GuidanceException;

        /**
         * Returns whether two different {@link ReferenceSymbolic}s refer to the same object
         * in the reached concrete state.
         * 
         * @param first a {@link ReferenceSymbolic}.
         * @param second a {@link ReferenceSymbolic}.
         * @return {@code true} iff {@code first} and {@code second} refer to the same
         *         object, or both refer to {@code null}.
         * @throws GuidanceException if {@code first} or {@code second} does not refer 
         *         to an object.
         */
        public abstract boolean areAlias(ReferenceSymbolic first, ReferenceSymbolic second) throws GuidanceException;

        /**
         * Returns the concrete value in the reached concrete state 
         * for a {@link Symbolic} value in the symbolic state.
         * 
         * @param origin a {@link Symbolic}.
         * @return a {@link Primitive} if {@code origin} is also {@link Primitive}, 
         *         otherwise a subclass-dependent object that "stands for" 
         *         the referred object, that must satisfy the property that, 
         *         if {@link #areAlias(ReferenceSymbolic, ReferenceSymbolic) areAlias}{@code (first, second)}, 
         *         then {@link #getValue(Symbolic) getValue}{@code (first).}{@link Object#equals(Object) equals}{@code (}{@link #getValue(Symbolic) getValue}{@code (second))}.
         * @throws GuidanceException if {@code origin} does not refer to an object.
         */
        public abstract Object getValue(Symbolic origin) throws GuidanceException;


        public Primitive eval_IFX(DecisionAlternative_IFX da, Primitive condition) throws GuidanceException {
            try {
                final Primitive conditionNot = this.calc.push(condition).not().pop();
                final Primitive conditionToCheck  = (da.value() ? condition : conditionNot);
                return eval(conditionToCheck);
            } catch (InvalidOperandException | InvalidTypeException e) {
                //this should never happen as arguments have been checked by the caller
                throw new UnexpectedInternalException(e);
            }
        }

        public Primitive eval_XCMPY(DecisionAlternative_XCMPY da, Primitive val1, Primitive val2) throws GuidanceException {
            try{
                final Primitive comparisonGT = this.calc.push(val1).gt(val2).pop();
                final Primitive comparisonEQ = this.calc.push(val1).eq(val2).pop();
                final Primitive comparisonLT = this.calc.push(val1).lt(val2).pop();
                final Primitive conditionToCheck  = 
                  (da.operator() == Operator.GT ? comparisonGT :
                   da.operator() == Operator.EQ ? comparisonEQ :
                   comparisonLT);
                return eval(conditionToCheck);
            } catch (InvalidTypeException | InvalidOperandException e) {
                //this should never happen as arguments have been checked by the caller
                throw new UnexpectedInternalException(e);
            }
        }

        public Primitive eval_XSWITCH(DecisionAlternative_XSWITCH da, Primitive selector, SwitchTable tab) throws GuidanceException {
            try {
                final Primitive conditionToCheck = (da.isDefault() ?
                                                    tab.getDefaultClause(this.calc, selector) :
                                                    this.calc.push(selector).eq(this.calc.valInt(da.value())).pop());
                return eval(conditionToCheck);
            } catch (InvalidOperandException | InvalidInputException | InvalidTypeException e) {
                //this should never happen as arguments have been checked by the caller
                throw new UnexpectedInternalException(e);
            }
        }

        public Primitive eval_XNEWARRAY(DecisionAlternative_XNEWARRAY da, Primitive countsNonNegative) throws GuidanceException {
            try {
                final Primitive conditionToCheck = (da.ok() ? countsNonNegative : this.calc.push(countsNonNegative).not().pop());
                return eval(conditionToCheck);
            } catch (InvalidOperandException | InvalidTypeException e) {
                //this should never happen as arguments have been checked by the caller
                throw new UnexpectedInternalException(e);
            }
        }

        public Primitive eval_XASTORE(DecisionAlternative_XASTORE da, Primitive inRange) throws GuidanceException {
            try {
                final Primitive conditionToCheck = (da.isInRange() ? inRange : this.calc.push(inRange).not().pop());
                return eval(conditionToCheck);
            } catch (InvalidOperandException | InvalidTypeException e) {
                //this should never happen as arguments have been checked by the caller
                throw new UnexpectedInternalException(e);
            }
        }

        public Primitive eval_XALOAD(DecisionAlternative_XALOAD da) throws GuidanceException {
            final Expression conditionToCheck = da.getArrayAccessExpressionSimplified();
            return (conditionToCheck == null ? this.calc.valBoolean(true) : eval(conditionToCheck));
        }
        
        /**
         * Evaluates a {@link Primitive} in the reached concrete state.
         * 
         * @param toEval a {@link Primitive}.
         * @return the {@link Primitive} corresponding to the concrete
         *         value of {@code toEval} in the reached concrete state
         *         (if {@code toEval instanceof }{@link Simplex} then
         *         the method will return {@code toEval}).
         * @throws GuidanceException
         */
        protected final Primitive eval(Primitive toEval) throws GuidanceException {
            final Evaluator evaluator = new Evaluator(this.calc, this);
            try {
                toEval.accept(evaluator);
            } catch (RuntimeException | GuidanceException e) {
                //do not stop them
                throw e;
            } catch (Exception e) {
                //should not happen
                throw new UnexpectedInternalException(e);
            }
            return evaluator.value;
        }

        private static final class Evaluator implements PrimitiveVisitor {
            private final Calculator calc;
            private final JVM jvm;
            Primitive value; //the result

            public Evaluator(Calculator calc, JVM jvm) {
                this.calc = calc;
                this.jvm = jvm;
            }

            @Override
            public void visitAny(Any x) {
                this.value = x;
            }

            @Override
            public void visitExpression(Expression e) throws Exception {
                if (e.isUnary()) {
                    e.getOperand().accept(this);
                    final Primitive operandValue = this.value;
                    if (operandValue == null) {
                        this.value = null;
                        return;
                    }
                    this.value = this.calc.push(operandValue).applyUnary(e.getOperator()).pop();
                } else {
                    e.getFirstOperand().accept(this);
                    final Primitive firstOperandValue = this.value;
                    if (firstOperandValue == null) {
                        this.value = null;
                        return;
                    }
                    e.getSecondOperand().accept(this);
                    final Primitive secondOperandValue = this.value;
                    if (secondOperandValue == null) {
                        this.value = null;
                        return;
                    }
                    this.value = this.calc.push(firstOperandValue).applyBinary(e.getOperator(), secondOperandValue).pop();
                }
            }

            @Override
            public void visitPrimitiveSymbolicApply(PrimitiveSymbolicApply x) throws Exception {
                final Object funValue = this.jvm.getValue(x);
                if (funValue instanceof Primitive) {
                    this.value = (Primitive) funValue;
                } else {
                    this.value = null;
                }
            }

            @Override
            public void visitPrimitiveSymbolicAtomic(PrimitiveSymbolicAtomic s) throws GuidanceException {
                final Object fieldValue = this.jvm.getValue(s);
                if (fieldValue instanceof Primitive) {
                    this.value = (Primitive) fieldValue;
                } else {
                    this.value = null;
                }
            }

            @Override
            public void visitSimplex(Simplex x) {
                this.value = x;
            }

            @Override
            public void visitTerm(Term x) {
                this.value = x;
            }

            @Override
            public void visitNarrowingConversion(NarrowingConversion x) throws Exception {
                x.getArg().accept(this);
                this.value = this.calc.push(this.value).narrow(x.getType()).pop();
            }

            @Override
            public void visitWideningConversion(WideningConversion x) throws Exception {
                x.getArg().accept(this);
                this.value = (x.getType() == this.value.getType() ? this.value : this.calc.push(this.value).widen(x.getType()).pop());
                //note that the concrete this.value could already be widened
                //because of conversion of actual types to computational types
                //through operand stack, see JVM specification v8, section 2.11.1, table 2.11.1-B
            }
        }

        protected abstract void step(State state) throws GuidanceException;

        protected abstract Signature getCurrentMethodSignature() throws ThreadStackEmptyException;

        protected abstract int getCurrentProgramCounter() throws ThreadStackEmptyException;

        protected void close() { }
    }

    public final void step(State state) throws GuidanceException {
        if (state.phase() == Phase.POST_INITIAL && this.guiding) {
            this.jvm.step(state);
        }
    }

    public Signature getCurrentMethodSignature() throws ThreadStackEmptyException {
        return this.jvm.getCurrentMethodSignature();
    }

    public int getCurrentProgramCounter() throws ThreadStackEmptyException {
        return this.jvm.getCurrentProgramCounter();
    }
}
