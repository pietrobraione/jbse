package jbse.dec;

import static jbse.bc.ClassLoaders.CLASSLOADER_APP;
import static jbse.common.Type.className;
import static jbse.common.Type.eraseGenericParameters;
import static jbse.common.Type.isTypeParameter;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.splitClassGenericSignatureTypeParameters;
import static jbse.common.Type.TYPEEND;
import static jbse.common.Type.TYPEVAR;
import static jbse.common.Type.typeParameterIdentifier;
import static jbse.mem.Util.isResolved;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.function.Supplier;

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
import jbse.common.Type;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.SolverEquationGenericTypes.Apply;
import jbse.dec.SolverEquationGenericTypes.Some;
import jbse.dec.SolverEquationGenericTypes.TypeTerm;
import jbse.dec.SolverEquationGenericTypes.Var;
import jbse.dec.exc.DecisionException;
import jbse.mem.Array;
import jbse.mem.Clause;
import jbse.mem.ClauseAssumeExpands;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.SwitchTable;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.tree.DecisionAlternative_XALOAD;
import jbse.tree.DecisionAlternative_XALOAD_Out;
import jbse.tree.DecisionAlternative_XALOAD_Resolved;
import jbse.tree.DecisionAlternative_XASTORE;
import jbse.tree.DecisionAlternative_XCMPY;
import jbse.tree.DecisionAlternative_XCMPY.Values;
import jbse.tree.DecisionAlternative_IFX;
import jbse.tree.DecisionAlternative_IFX_False;
import jbse.tree.DecisionAlternative_IFX_True;
import jbse.tree.DecisionAlternative_JAVA_MAP;
import jbse.tree.DecisionAlternative_XLOAD_GETX;
import jbse.tree.DecisionAlternative_XLOAD_GETX_Expands;
import jbse.tree.DecisionAlternative_XLOAD_GETX_Resolved;
import jbse.tree.DecisionAlternative_XNEWARRAY;
import jbse.tree.DecisionAlternative_XNEWARRAY_Ok;
import jbse.tree.DecisionAlternative_XNEWARRAY_Wrong;
import jbse.tree.DecisionAlternative_XSWITCH;
import jbse.val.Any;
import jbse.val.Calculator;
import jbse.val.Expression;
import jbse.val.HistoryPoint;
import jbse.val.KlassPseudoReference;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.ReferenceSymbolic;
import jbse.val.ReferenceSymbolicApply;
import jbse.val.ReferenceSymbolicAtomic;
import jbse.val.ReferenceSymbolicMember;
import jbse.val.ReferenceSymbolicMemberArray;
import jbse.val.ReferenceSymbolicMemberMapValue;
import jbse.val.Simplex;
import jbse.val.Term;
import jbse.val.Value;
import jbse.val.WideningConversion;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * A {@link DecisionProcedureAlgorithms} decorates a {@link DecisionProcedure} 
 * by adding to it a number of higher-level methods which ease the calculations 
 * of {@link DecisionAlternative}s.
 * 
 * @author Pietro Braione
 */
public class DecisionProcedureAlgorithms extends DecisionProcedureDecorator {
    /**
     * Enum for the possible (info) outcomes of a decision.
     * 
     * @author Pietro Braione
     */
    public enum Outcome {
        /** Should refine, branching decision. */
        TT(true, true), 

        /** Should refine, nonbranching decision. */
        TF(true, false), 

        /** Should not refine, branching decision. */
        FT(false, true),

        /** Should not refine, nonbranching decision. */
        FF(false, false),

        /** Should refine, no reference expansion, branching decision. */
        TTT(true, true, true),

        /** Should refine, no reference expansion, nonbranching decision. */
        TTF(true, true, false),

        /** Should refine, reference expansion, branching decision. */
        TFT(true, false, true),

        /** Should refine, reference expansion, nonbranching decision. */
        TFF(true, false, false),

        /** Should not refine, no reference expansion, branching decision. */
        FTT(false, true, true),

        /** Should not refine, no reference expansion, nonbranching decision. */
        FTF(false, true, false),

        /** Should not refine, reference expansion, branching decision. */
        FFT(false, false, true),

        /** Should not refine, reference expansion, nonbranching decision. */
        FFF(false, false, false);

        private final boolean shouldRefine;
        private final boolean partialReferenceResolution;
        private final boolean branchingDecision;

        private Outcome(boolean shouldRefine, boolean branchingDecision) {
            this.shouldRefine = shouldRefine;
            this.partialReferenceResolution = false;
            this.branchingDecision = branchingDecision;
        }

        private Outcome(boolean shouldRefine, boolean noReferenceExpansion, boolean branchingDecision) {
            this.shouldRefine = shouldRefine;
            this.partialReferenceResolution = noReferenceExpansion;
            this.branchingDecision = branchingDecision;
        }

        /**
         * Returns an outcome (reference resolution does not apply).
         * 
         * @param shouldRefine {@code true} iff should refine.
         * @param branching {@code true} iff the decision is a 
         *        branching one. 
         * @return a suitable outcome,  
         *         encapsulating the values of {@code shouldRefine}
         *         and {@code branching}.
         */
        public static Outcome val(boolean shouldRefine, boolean branching) {
            if (shouldRefine && branching) {
                return TT;
            } else if (shouldRefine && !branching) {
                return TF;
            } else if (!shouldRefine && branching) {
                return FT;
            } else { //if (!shouldRefine && !branching)
                return FF;
            }
        }

        /**
         * Returns an outcome (reference resolution applies).
         * 
         * @param shouldRefine {@code true} iff should refine.
         * @param noReferenceExpansion {@code true} iff 
         *        the reference has not been
         *        expanded (it is partially resolved).
         * @param branching {@code true} iff the decision is a 
         *        branching one. 
         * @return a suitable outcome,  
         *         encapsulating the values of {@code shouldRefine},
         *         {@code partialReferenceResolution} and {@code branching}.
         */
        public static Outcome val(boolean shouldRefine, boolean noReferenceExpansion, boolean branching) {
            if (shouldRefine && noReferenceExpansion && branching) {
                return TTT;
            } else if (shouldRefine && noReferenceExpansion && !branching) {
                return TTF;
            } else if (shouldRefine && !noReferenceExpansion && branching) {
                return TFT;
            } else if (shouldRefine && !noReferenceExpansion && !branching) {
                return TFF;
            } else if (!shouldRefine && noReferenceExpansion && branching) {
                return FTT;
            } else if (!shouldRefine && noReferenceExpansion && !branching) {
                return FTF;
            } else if (!shouldRefine && !noReferenceExpansion && branching) {
                return FFT;
            } else { //if (!shouldRefine && !partialReferenceResolution && !branching)
                return FFF;
            }
        }

        /**
         * Should the generated states be refined?
         * 
         * @return {@code true} iff the generated states must be refined;
         *         this happens when there is more than one outcome, or
         *         some symbolic reference has been expanded.
         */
        public boolean shouldRefine() {
            return this.shouldRefine;
        }

        /**
         * Is a reference resolution partial?
         * 
         * @return {@code true} iff a symbolic reference resolution is suspect because
         *         <em>partial</em>, i.e., the reference was not resolved
         *         by expansion because no concrete class was found in the classpath 
         *         that is type-compatible with the symbolic reference and assumed to 
         *         be not pre-initialized.
         */
        public boolean partialReferenceResolution() {
            if (this == TT || this == TF || this == FT || this == FF) {
                throw new UnexpectedInternalException(this.toString() + " carries no partial reference resolution information."); //TODO throw a better exception
            }
            return this.partialReferenceResolution;
        }

        /**
         * Was the decision a branching one? 
         * 
         * @return {@code true} iff the decision taken 
         *         was <em>branching</em>, i.e., it has
         *         not a single, fixed outcome.
         */
        public boolean branchingDecision() {
            return this.branchingDecision;
        }
    }
    
    protected Supplier<State> currentStateSupplier;
    protected final Calculator calc;

    public DecisionProcedureAlgorithms(DecisionProcedure component) 
    throws InvalidInputException {
        super(component);
        this.calc = getCalculator();
    }
    
    @Override
    public void setCurrentStateSupplier(Supplier<State> currentStateSupplier) {
        this.currentStateSupplier = currentStateSupplier;
    }

    /**
     * Decides a condition for "branch if integer comparison" bytecodes.
     * 
     * @param calc a {@link Calculator}. It must not be {@code null}.
     * @param condition a {@link Primitive} representing a logical value or clause.
     *        It must not be {@code null}.
     * @param result a {@link SortedSet}{@code <}{@link DecisionAlternative_IFX}{@code >}, 
     *            where the method will put a {@link DecisionAlternative_IFX_True} object
     *            iff {@code condition} does not contradict the current assumptions, and 
     *            a {@link DecisionAlternative_IFX_False} object iff
     *            {@code condition.}{@link Primitive#not() not()} is an {@link Expression} that
     *            does not contradict the current assumptions. Note that the two situations
     *            are not mutually exclusive (they are if {@code condition} is concrete).
     * @return an {@link Outcome}.
     * @throws InvalidInputException when one of the parameters is incorrect.
     * @throws DecisionException upon failure.
     */
    //TODO should be final?
    public Outcome decide_IFX(Primitive condition, SortedSet<DecisionAlternative_IFX> result)
    throws InvalidInputException, DecisionException {
        if (condition == null || result == null) {
            throw new InvalidInputException("decide_IFX invoked with a null parameter.");
        }
        if (condition.getType() != Type.BOOLEAN) {
            throw new InvalidInputException("decide_IFX condition has type " + condition.getType());
        }
        if (condition instanceof Simplex) {
            decide_IFX_Concrete((Simplex) condition, result);
            return Outcome.FF;
        } else {		
            final Outcome o = decide_IFX_Nonconcrete(condition, result);
            return o;
        }
    }

    private void decide_IFX_Concrete(Simplex condition, SortedSet<DecisionAlternative_IFX> result) {
        final boolean conditionBoolean = (Boolean) condition.getActualValue();
        result.add(DecisionAlternative_IFX.toConcrete(conditionBoolean));
    }

    private boolean isAny(Primitive p) {
        return (p instanceof Any || 
               (p instanceof WideningConversion && ((WideningConversion) p).getArg() instanceof Any));
    }

    protected Outcome decide_IFX_Nonconcrete(Primitive condition, SortedSet<DecisionAlternative_IFX> result) 
    throws DecisionException {	
        final boolean shouldRefine;
        final DecisionAlternative_IFX T = DecisionAlternative_IFX.toNonconcrete(true);
        final DecisionAlternative_IFX F = DecisionAlternative_IFX.toNonconcrete(false);

        //TODO what if condition is neither Simplex, nor Any, nor Expression (i.e., FunctionApplication, Widening/NarrowingConversion, PrimitiveSymbolic, Term)?
        try {
            final Expression exp = (Expression) condition; 
            //this implementation saves one sat check in 50% cases
            //(it exploits the fact that if exp is unsat 
            //exp.not() is valid)
            if (isAny(exp.getFirstOperand()) || isAny(exp.getSecondOperand())) {
                result.add(T);
                result.add(F);
                shouldRefine = false; //any-based conditions shall not reach the decision procedure
            } else if (isSat(exp)) {
                result.add(T);
                final Expression expNot = (Expression) this.calc.push(condition).not().pop(); 
                if (isSat(expNot)) {
                    result.add(F);
                }
                shouldRefine = (result.size() > 1);
            } else {
                //exp is unsat, thus its negation is valid
                result.add(F);
                shouldRefine = false;
            }
        } catch (InvalidOperandException | InvalidTypeException | InvalidInputException e) {
            //this should never happen as arguments have been checked by the caller
            throw new UnexpectedInternalException(e);
        }
        return Outcome.val(shouldRefine, true);
    }

    /**
     * Decides a comparison for comparison bytecodes.
     * 
     * @param val1 a {@link Primitive}. It must not be {@code null}.
     * @param val2 another {@link Primitive}. It must not be {@code null}.
     * @param result a {@link SortedSet}{@code <}{@link DecisionAlternative_XCMPY}{@code >}, 
     *            which the method will update by adding to it a {@link DecisionAlternative_XCMPY_Gt} object 
     *            (respectively, {@link DecisionAlternative_XCMPY_Eq}, {@link DecisionAlternative_XCMPY_Lt})
     *            iff {@code val1} greater than {@code val2} (respectively, {@code val1} equal to {@code val2}, 
     *            {@code val1} less than {@code val2}) does not contradict the current assumptions. 
     *            Note that the three conditions are not necessarily mutually exclusive (they are when 
     *            {@code val1} and {@code val2} are concrete).
     * @return an {@link Outcome}.
     * @throws InvalidInputException when one of the parameters is incorrect.
     * @throws DecisionException upon failure.
     */
    //TODO should be final?
    public Outcome decide_XCMPY(Primitive val1, Primitive val2, SortedSet<DecisionAlternative_XCMPY> result)
    throws InvalidInputException, DecisionException {
        if (val1 == null || val2 == null || result == null) {
            throw new InvalidInputException("decide_XCMPY invoked with a null parameter.");
        }
        try {
            Operator.typeCheck(Operator.EQ, val1.getType(), val2.getType());
        } catch (InvalidTypeException e) {
            throw new InvalidInputException("decide_XCMPY invoked with noncomparable parameters.");
        }
        if ((val1 instanceof Simplex) && (val2 instanceof Simplex)) {
            decide_XCMPY_Concrete((Simplex) val1, (Simplex) val2, result);
            return Outcome.FF;
        } else {
            final Outcome o = decide_XCMPY_Nonconcrete(val1, val2, result);
            return o;
        }
    }

    private void decide_XCMPY_Concrete(Simplex val1, Simplex val2, SortedSet<DecisionAlternative_XCMPY> result) {
        try {
            final Simplex conditionGt = (Simplex) this.calc.push(val1).gt(val2).pop();
            final boolean conditionGtValue = (Boolean) conditionGt.getActualValue();
            if (conditionGtValue) {
                result.add(DecisionAlternative_XCMPY.toConcrete(Values.GT));
            } else {
                final Simplex conditionEq = (Simplex) this.calc.push(val1).eq(val2).pop();
                final boolean conditionEqValue = (Boolean) conditionEq.getActualValue();
                if (conditionEqValue) {
                    result.add(DecisionAlternative_XCMPY.toConcrete(Values.EQ));
                } else {
                    result.add(DecisionAlternative_XCMPY.toConcrete(Values.LT));
                }
            }
        } catch (InvalidTypeException | InvalidOperandException e) {
            //this should never happen as arguments have been checked by the caller
            throw new UnexpectedInternalException(e);
        }
    }

    protected Outcome decide_XCMPY_Nonconcrete(Primitive val1, Primitive val2, SortedSet<DecisionAlternative_XCMPY> result) 
    throws DecisionException {
        final boolean shouldRefine;
        final DecisionAlternative_XCMPY GT = DecisionAlternative_XCMPY.toNonconcrete(Values.GT);
        final DecisionAlternative_XCMPY EQ = DecisionAlternative_XCMPY.toNonconcrete(Values.EQ);
        final DecisionAlternative_XCMPY LT = DecisionAlternative_XCMPY.toNonconcrete(Values.LT);

        if (isAny(val1) || isAny(val2)) {
            //1 - condition involving "don't care" values
            result.add(GT);
            result.add(EQ);
            result.add(LT);
            shouldRefine = false; //any-based conditions shall not reach the decision procedure
        } else {
            try {
                final Expression expGT = (Expression) this.calc.push(val1).gt(val2).pop();
                final Expression expEQ = (Expression) this.calc.push(val1).eq(val2).pop();
                final Expression expLT = (Expression) this.calc.push(val1).lt(val2).pop();

                //this implementation saves one sat check in 33% cases
                //(it exploits the fact that if both val1 > val2 and 
                //val1 = val2 are unsat, then val1 < val2 is valid)
                if (isSat(expGT)) {
                    result.add(GT);
                    if (isSat(expEQ)) {
                        result.add(EQ);
                    }
                    if (isSat(expLT)) {
                        result.add(LT); 
                    }
                } else if (isSat(expEQ)) { //expGT is unsat, so either expEQ or expLT, or both, are SAT 
                    result.add(EQ);
                    if (isSat(expLT)) {
                        result.add(LT); 
                    }
                } else {
                    //both expGT and expEQ are unsat; so expLT is valid
                    result.add(LT);
                }
                shouldRefine = (result.size() > 1);
            } catch (InvalidTypeException | InvalidOperandException | InvalidInputException e) {
                //this should never happen as arguments have been checked by the caller
                throw new UnexpectedInternalException(e);
            }
        }
        return Outcome.val(shouldRefine, true);
    }

    /**
     * Decides a table or a range for switch bytecodes.
     * 
     * @param selector a {@link Primitive} with type int. 
     * @param tab a {@link SwitchTable}.
     * @param result a {@link SortedSet}{@code <}{@link DecisionAlternative_XSWITCH}{@code >} 
     *            where the method will put the {@link DecisionAlternative_XSWITCH} objects {@code s} 
     *            such that the equality of {@code selector.}{@link Primitive#eq(Primitive) eq}{@code (s.}{@link DecisionAlternative_XSWITCH#value() value()}{@code )}
     *            does not contradict the current assumptions.
     * @return an {@link Outcome}.
     * @throws InvalidInputException when one of the parameters is incorrect.
     * @throws DecisionException upon failure.
     */
    //TODO should be final?
    public Outcome decide_XSWITCH(Primitive selector, SwitchTable tab, SortedSet<DecisionAlternative_XSWITCH> result)
    throws InvalidInputException, DecisionException {
        if (selector == null || tab == null || result == null) {
            throw new InvalidInputException("decide_XSWITCH invoked with a null parameter.");
        }
        if (selector.getType() != Type.INT) {
            throw new InvalidInputException("decide_XSWITCH selector has type " + selector.getType());
        }
        if (selector instanceof Simplex) {
            decide_XSWITCH_Concrete((Simplex) selector, tab, result);
            return Outcome.FF;
        } else {
            final Outcome o = decide_XSWITCH_Nonconcrete(selector, tab, result);
            return o;
        }
    }

    private void decide_XSWITCH_Concrete(Simplex selector, SwitchTable tab, SortedSet<DecisionAlternative_XSWITCH> result) {
        final int opValue = (Integer) selector.getActualValue();
        for (int i : tab) {
            if (i == opValue) { 
                result.add(DecisionAlternative_XSWITCH.toConcrete(i));
                return;
            }
        }
        //not found
        result.add(DecisionAlternative_XSWITCH.toConcreteDefault());
    }

    protected Outcome decide_XSWITCH_Nonconcrete(Primitive selector, SwitchTable tab, SortedSet<DecisionAlternative_XSWITCH> result) 
    throws DecisionException {
        try {
            final boolean isAny = isAny(selector);
            boolean noEntryIsSat = true; //allows to skip the last sat check
            for (int i : tab) {
                final Expression exp = (isAny ? null : (Expression) this.calc.push(selector).eq(this.calc.valInt(i)).pop());
                if (isAny || isSat(exp)) { 
                    result.add(DecisionAlternative_XSWITCH.toNonconcrete(i));
                    noEntryIsSat = false;
                }
            }
            if (isAny || noEntryIsSat || isSat(tab.getDefaultClause(this.calc, selector))) { 
                result.add(DecisionAlternative_XSWITCH.toNonconcreteDefault());
            }
            final boolean shouldRefine = (!isAny && (result.size() > 1));
            return Outcome.val(shouldRefine, true);
        } catch (InvalidOperandException | InvalidTypeException | InvalidInputException e) {
            //this should never happen as arguments have been checked by the caller
            throw new UnexpectedInternalException(e);
        }
    }

    /**
     * Decides array creation.
     * 
     * @param countsNonNegative a {@link Primitive} expressing the fact that the count 
     *        values popped from the operand stack are nonnegative.
     * @param result a {@link SortedSet}{@code <}{@link DecisionAlternative_XNEWARRAY}{@code >}, which the method 
     *            will update by adding to it a {@link DecisionAlternative_XNEWARRAY_Ok} 
     *            (respectively, a {@link DecisionAlternative_XNEWARRAY_Wrong}) in the case
     *            a successful (respectively, unsuccessful) creation of the array with
     *            the provided count values does not contradict the current assumptions.
     *            Note that the two situations are not mutually exclusive.
     * @return an {@link Outcome}.
     * @throws InvalidInputException when one of the parameters is incorrect.
     * @throws DecisionException upon failure.
     */
    //TODO should be final?
    public Outcome decide_XNEWARRAY(Primitive countsNonNegative, SortedSet<DecisionAlternative_XNEWARRAY> result) 
    throws InvalidInputException, DecisionException {
        if (countsNonNegative == null || result == null) {
            throw new InvalidInputException("decide_XNEWARRAY invoked with a null parameter");
        }
        if (countsNonNegative.getType() != Type.BOOLEAN) {
            throw new InvalidInputException("decide_XNEWARRAY countsNonNegative type is " + countsNonNegative.getType());
        }
        if (countsNonNegative instanceof Simplex) {
            decide_XNEWARRAY_Concrete((Simplex) countsNonNegative, result);
            return Outcome.FF;
        } else {
            final Outcome o = decide_XNEWARRAY_Nonconcrete(countsNonNegative, result);
            return o;
        }
    }

    private void decide_XNEWARRAY_Concrete(Simplex countsNonNegative, SortedSet<DecisionAlternative_XNEWARRAY> result) {
        final boolean countsNonNegativeBoolean = (Boolean) countsNonNegative.getActualValue();
        result.add(DecisionAlternative_XNEWARRAY.toConcrete(countsNonNegativeBoolean));
    }

    protected Outcome decide_XNEWARRAY_Nonconcrete(Primitive countsNonNegative, SortedSet<DecisionAlternative_XNEWARRAY> result) 
    throws DecisionException {
        final boolean shouldRefine;
        final DecisionAlternative_XNEWARRAY OK = DecisionAlternative_XNEWARRAY.toNonconcrete(true);
        final DecisionAlternative_XNEWARRAY WRONG = DecisionAlternative_XNEWARRAY.toNonconcrete(false);

        if (isAny(countsNonNegative)) {
            //TODO can it really happen? should we throw an exception in the case?
            result.add(WRONG);
            result.add(OK);
            shouldRefine = false;
        } else {
            //TODO what if condition is neither Simplex, nor Any, nor Expression (i.e., FunctionApplication, Widening/NarrowingConversion, PrimitiveSymbolic, Term)?
            try {
                //this implementation saves one sat check in 50% cases
                //(it exploits the fact that if exp is unsat 
                //exp.not() is valid)
                final Expression negative = (Expression) this.calc.push(countsNonNegative).not().pop(); 
                if (isSat(negative)) {
                    result.add(WRONG);
                    final Expression nonNegative = (Expression) countsNonNegative;
                    if (isSat(nonNegative)) {
                        result.add(OK);
                    }
                } else {
                    result.add(OK);
                }
                shouldRefine = (result.size() > 1);
            } catch (InvalidOperandException | InvalidTypeException | InvalidInputException e) {
                //this should never happen as arguments have been checked by the caller
                throw new UnexpectedInternalException(e);
            }
        }
        return Outcome.val(shouldRefine, true);
    }

    /**
     * Decides a store to an array.
     * 
     * @param inRange a {@link Primitive} expressing the fact that the access
     *        index is in the interval 0..array.length. 
     * @param result a {@link SortedSet}{@code <}{@link DecisionAlternative_XASTORE}{@code >}, which the method 
     *            will update by adding to it {@link DecisionAlternative_XASTORE#IN} 
     *            or {@link DecisionAlternative_XASTORE#OUT} in the case the access may be 
     *            in range or out of range. Note that the two situations are not
     *            mutually exclusive.
     * @return an {@link Outcome}.
     * @throws InvalidInputException when one of the parameters is incorrect.
     * @throws DecisionException upon failure.
     */
    public Outcome decide_XASTORE(Primitive inRange, SortedSet<DecisionAlternative_XASTORE> result)
    throws InvalidInputException, DecisionException {
        if (inRange == null || result == null) {
            throw new InvalidInputException("decide_XASTORE invoked with a null parameter");
        }
        if (inRange.getType() != Type.BOOLEAN) {
            throw new InvalidInputException("decide_XASTORE inRange type is " + inRange.getType());
        }
        if (inRange instanceof Simplex) {
            decide_XASTORE_Concrete((Simplex) inRange, result);
            return Outcome.FF;
        } else {
            final Outcome o = decide_XASTORE_Nonconcrete(inRange, result);
            return o;
        }
    }

    private void decide_XASTORE_Concrete(Simplex inRange, SortedSet<DecisionAlternative_XASTORE> result) {
        final boolean inRangeBoolean = (Boolean) inRange.getActualValue();
        result.add(DecisionAlternative_XASTORE.toConcrete(inRangeBoolean));
    }

    protected Outcome decide_XASTORE_Nonconcrete(Primitive inRange, SortedSet<DecisionAlternative_XASTORE> result)
    throws DecisionException {
        final boolean shouldRefine;
        final DecisionAlternative_XASTORE IN = DecisionAlternative_XASTORE.toNonconcrete(true);
        final DecisionAlternative_XASTORE OUT = DecisionAlternative_XASTORE.toNonconcrete(false);

        if (isAny(inRange)) {
            //TODO can it really happen? should we throw an exception in the case?
            result.add(OUT);
            result.add(IN);
            shouldRefine = false;
        } else {
            try {
                //this implementation saves one sat check in 50% cases
                //(it exploits the fact that if exp is unsat 
                //exp.not() is valid)
                final Expression outOfRangeExp = (Expression) this.calc.push(inRange).not().pop();
                if (isSat(outOfRangeExp)) {
                    result.add(OUT);
                    final Expression inRangeExp = (Expression) inRange;
                    if (isSat(inRangeExp)) {
                        result.add(IN);
                    }
                } else {
                    result.add(IN);			
                }
                shouldRefine = (result.size() > 1);
            } catch (InvalidOperandException | InvalidTypeException | InvalidInputException e) {
                //this should never happen as arguments have been checked by the caller
                throw new UnexpectedInternalException(e);
            }

        }
        return Outcome.val(shouldRefine, true);
    }
    
    /**
     * Decides an access to a model map.
     * 
     * @param predicates
     * @param result
     * @return
     * @throws InvalidInputException
     * @throws DecisionException
     */
    public Outcome decide_JAVA_MAP(Primitive[] predicates, SortedSet<DecisionAlternative_JAVA_MAP> result) 
    throws InvalidInputException, DecisionException {
    	if (predicates == null || result == null) {
            throw new InvalidInputException("decide_JAVA_MAP invoked with a null parameter.");
    	}
    	boolean allSimplex = true;
    	for (Primitive predicate : predicates) {
    		if (predicate == null) {
                throw new InvalidInputException("decide_JAVA_MAP invoked with a null element in the parameter Primitive[] predicates.");
    		}
    		if (predicate instanceof Simplex) {
    			//do nothing
    		} else {
    			allSimplex = false;
    			break;
    		}
    	}
    	
    	if (allSimplex) {
    		final Simplex[] conditions = new Simplex[predicates.length];
    		for (int i = 0; i < predicates.length; ++i) {
    			conditions[i] = (Simplex) predicates[i];
    		}
    		decide_JAVA_MAP_Concrete(conditions, result);
    		return Outcome.val(false, result.size() > 1);
    	} else {
    		return decide_JAVA_MAP_Nonconcrete(predicates, result);
    	}
    }
    
    private void decide_JAVA_MAP_Concrete(Simplex[] conditions, SortedSet<DecisionAlternative_JAVA_MAP> result) 
    throws InvalidInputException {
    	final Simplex valTrue = this.calc.valBoolean(true);
		for (int i = 0; i < conditions.length; ++i) {
			if (conditions[i].surelyTrue()) {
				result.add(new DecisionAlternative_JAVA_MAP(valTrue, i));
			}
		}
    }
    
    protected Outcome decide_JAVA_MAP_Nonconcrete(Primitive[] predicates, SortedSet<DecisionAlternative_JAVA_MAP> result) 
    throws InvalidInputException, DecisionException {
		for (int i = 0; i < predicates.length; ++i) {
			//we assume that predicates contains only Expressions
			final Expression exp = (Expression) predicates[i];
			if (isSat(exp)) {
				result.add(new DecisionAlternative_JAVA_MAP(exp, i));
			}
		}
		return Outcome.val(true, result.size() > 1);
    }

    /**
     * Resolves loading a value from a local variable or a field to the operand stack.
     * 
     * @param valToLoad the {@link Value} returned by the local variable access, 
     *        that must be loaded on the operand stack. It must not be {@code null}.
     * @param result a {@link SortedSet}{@code <}{@link DecisionAlternative_XLOAD_GETX}{@code >}, 
     *        where the method will put all the {@link DecisionAlternative_XLOAD_GETX}s 
     *        representing all the satisfiable outcomes of the operation.
     * @return an {@link Outcome}.
     * @throws InvalidInputException when one of the parameters is incorrect.
     * @throws DecisionException upon failure.
     * @throws ClassFileNotFoundException if {@code valToLoad} is a symbolic 
     *         reference and 
     *         {@code valToLoad.}{@link Signature#getClassName() getClassName()}
     *         or the class name of one of its possibile expansions does 
     *         not have a classfile in {@code state}'s classpath.
     * @throws ClassFileIllFormedException if {@code valToLoad} is a symbolic 
     *         reference and the classfile for
     *         {@code valToLoad.}{@link Signature#getClassName() getClassName()}
     *         or for the class name of one of its possibile expansions is ill-formed.
     * @throws BadClassFileVersionException when {@code valToLoad} is a symbolic 
     *         reference and the bytecode for
     *         {@code valToLoad.}{@link Signature#getClassName() getClassName()}
     *         or for the class name of one of its possibile expansions has a version
     *         number that is unsupported by this version of JBSE.
     * @throws RenameUnsupportedException if {@code valToLoad} is a symbolic 
     *         reference and the class for 
     *         {@code valToLoad.}{@link Signature#getClassName() getClassName()}
     *         or the class of one of its possibile expansions derives from a 
     *         model class but the classfile does not support renaming.
     * @throws WrongClassNameException when {@code valToLoad} is a symbolic 
     *         reference and the bytecode for
     *         {@code valToLoad.}{@link Signature#getClassName() getClassName()}
     *         or for the class name of one of its possibile expansions has a 
     *         class name different from the expected one ({@code valToLoad.}{@link Signature#getClassName() getClassName()}
     *         or the expansion's name).
     * @throws IncompatibleClassFileException if {@code valToLoad} is a symbolic 
     *         reference and the superclass of class
     *         {@code valToLoad.}{@link Signature#getClassName() getClassName()}
     *         is resolved to an interface class, or any superinterface is resolved
     *         to an object class.
     * @throws ClassFileNotAccessibleException if {@code valToLoad} is a symbolic 
     *         reference and the classfile for
     *         {@code valToLoad.}{@link Signature#getClassName() getClassName()}
     *         or for the class name of one of its possibile expansions cannot access
     *         one of its superclass/superinterfaces.
     * @throws ContradictionException in the case of guidance procedures during class 
     *         initialization of backdoor expansions.
     * @throws InterruptException in the case of guidance procedures during class 
     *         initialization of backdoor expansions.
     * @throws HeapMemoryExhaustedException in the case of guidance procedures during class 
     *         initialization of backdoor expansions.
     * @throws ClasspathException in the case of guidance procedures during class 
     *         initialization of backdoor expansions.
     */
    public Outcome resolve_XLOAD_GETX(Value valToLoad, SortedSet<DecisionAlternative_XLOAD_GETX> result) 
    throws InvalidInputException, DecisionException, ClassFileNotFoundException, ClassFileIllFormedException, 
    BadClassFileVersionException, RenameUnsupportedException, WrongClassNameException, 
    IncompatibleClassFileException, ClassFileNotAccessibleException, ClasspathException, HeapMemoryExhaustedException,
    InterruptException, ContradictionException {
        if (valToLoad == null || result == null) {
            throw new InvalidInputException("resolve_XLOAD_GETX invoked with a null parameter.");
        }
        if (isResolved(getAssumptions(), valToLoad)) {
            result.add(new DecisionAlternative_XLOAD_GETX_Resolved(valToLoad));
            return Outcome.FFF;
        } else { 
            return resolve_XLOAD_GETX_Unresolved(this.currentStateSupplier.get().getClassHierarchy(), (ReferenceSymbolic) valToLoad, result);
        }
    }

    /**
     * Resolves loading a value from a local variable or a field to the operand stack,
     * in the case the value to load is an unresolved symbolic
     * reference.
     * 
     * @param hier a {@link ClassHierarchy}.
     * @param refToLoad the {@link ReferenceSymbolic} returned by the local variable access, 
     *        that must be loaded on {@code state}'s operand stack. It must not be {@code null}.
     * @param result a {@link SortedSet}{@code <}{@link DecisionAlternative_XLOAD_GETX}{@code >}, 
     *        where the method will put all the {@link DecisionAlternative_XLOAD_GETX}s 
     *        representing all the satisfiable outcomes of the operation.
     * @return an {@link Outcome}.
     * @throws DecisionException upon failure.
     * @throws ClassFileNotFoundException if 
     *         {@code refToLoad.}{@link Signature#getClassName() getClassName()}
     *         or the class name of one of its possibile expansions does 
     *         not have a classfile in the classpath.
     * @throws ClassFileIllFormedException if the classfile for
     *         {@code refToLoad.}{@link Signature#getClassName() getClassName()}
     *         or for the class name of one of its possibile expansions is ill-formed.
     * @throws BadClassFileVersionException when the bytecode for
     *         {@code refToLoad.}{@link Signature#getClassName() getClassName()}
     *         or for the class name of one of its possibile expansions has a version
     *         number that is unsupported by this version of JBSE.
     * @throws RenameUnsupportedException when {@code refToLoad.}{@link Signature#getClassName() getClassName()}
     *         or the class of one of its possibile expansions derives from a 
     *         model class but the classfile does not support renaming.
     * @throws WrongClassNameException when the bytecode for
     *         {@code refToLoad.}{@link Signature#getClassName() getClassName()}
     *         or for the class name of one of its possibile expansions has a 
     *         class name different from the expected one ({@code refToLoad.}{@link Signature#getClassName() getClassName()}
     *         or the expansion's name).
     * @throws IncompatibleClassFileException if the superclass of
     *         {@code refToLoad.}{@link Signature#getClassName() getClassName()}
     *         or of one of its possible expansion is resolved to an interface class,
     *         or any superinterface is resolved to an object class.
     * @throws ClassFileNotAccessibleException if the classfile for
     *         {@code refToLoad.}{@link Signature#getClassName() getClassName()}
     *         or for the class name of one of its possibile expansions cannot access
     *         one of its superclass/superinterfaces.
     * @throws ContradictionException in the case of guidance procedures during class 
     *         initialization of backdoor expansions.
     * @throws InterruptException in the case of guidance procedures during class 
     *         initialization of backdoor expansions.
     * @throws HeapMemoryExhaustedException in the case of guidance procedures during class 
     *         initialization of backdoor expansions.
     * @throws ClasspathException in the case of guidance procedures during class 
     *         initialization of backdoor expansions.
     * @see {@link #resolve_XLOAD_GETX(Value, SortedSet) resolve_XLOAD_GETX}.
     */
    protected Outcome resolve_XLOAD_GETX_Unresolved(ClassHierarchy hier, ReferenceSymbolic refToLoad, SortedSet<DecisionAlternative_XLOAD_GETX> result)
    throws DecisionException, ClassFileNotFoundException, ClassFileIllFormedException, 
    BadClassFileVersionException, RenameUnsupportedException, WrongClassNameException, 
    IncompatibleClassFileException, ClassFileNotAccessibleException, ClasspathException, HeapMemoryExhaustedException, InterruptException, ContradictionException {
        try {
            final boolean partialReferenceResolution = 
            doResolveReference(hier, refToLoad, new DecisionAlternativeReferenceFactory_XLOAD_GETX(), result);
            return Outcome.val(true, partialReferenceResolution, true); //uninitialized symbolic references always require a refinement action
        } catch (InvalidInputException e) {
            //this should never happen as arguments have been checked by the caller
            throw new UnexpectedInternalException(e);
        }
    }
    
    /**
     * Class whose instances represent information about an
     * array reading access.
     * 
     * @author Pietro Braione
     *
     */
    public static final class ArrayAccessInfo {
        public final Reference sourceArrayReference;
        public final Expression accessExpression;
        public final Term indexFormal;
        public final Primitive indexActual;
        public final Value readValue;
        public final boolean fresh;

        /**
         * Constructor.
         * 
         * @param sourceArrayReference when {@code fresh == true} is a {@link Reference} to the {@link Array} 
         *        that is being read. When {@code fresh == false} it can be {@code null}.
         * @param accessExpression an {@link Expression} containing {@code indexFormal}, 
         *        signifying the condition under which the array access yields {@code readValue} 
         *        as result. It can be {@code null}, in which case it is equivalent to {@code true} but 
         *        additionally denotes the fact that the array was accessed by a concrete index.
         * @param indexFormal the {@link Term} used in {@code accessExpression} to indicate
         *        the array index. It must not be {@code null}.
         * @param indexActual a {@link Primitive}, the actual index used to access the array.
         *        It must not be {@code null}.
         * @param readValue the {@link Value} returned by the array access when 
         *        {@code accessExpression} is true, or {@code null} to denote an 
         *        access out of the array bounds.
         * @param fresh {@code true} iff {@code valToLoad} is fresh, i.e., 
         *        it was assumed during the array access and thus it 
         *        is not yet stored in the {@link Array} it originates from.
         */
        public ArrayAccessInfo(Reference sourceArrayReference, Expression accessExpression, Term indexFormal, Primitive indexActual, Value readValue, boolean fresh) {
            this.sourceArrayReference = sourceArrayReference;
            this.accessExpression = accessExpression;
            this.indexFormal = indexFormal;
            this.indexActual = indexActual;
            this.readValue = readValue;
            this.fresh = fresh;
        }
    }

    /**
     * Resolves loading a value to the operand stack, when the value
     * comes from an array.
     * 
     * @param arrayAccessInfos a {@link List}{@code <}{@link ArrayAccessInfo}{@code >}.  
     *        It must not be {@code null}.
     * @param result a {@link SortedSet}{@code <}{@link DecisionAlternative_XALOAD}{@code >}, 
     *        where the method will put all the 
     *        {@link DecisionAlternative_XALOAD}s representing all the 
     *        satisfiable outcomes of the operation. It must not be {@code null}.
     * @param nonExpandedRefs a {@link List}{@code <}{@link ReferenceSymbolic}{@code >} 
     *        that this method will populate with the {@link ReferenceSymbolic}s that 
     *        were not expanded. It must not be {@code null}.
     * @return an {@link Outcome}.
     * @throws InvalidInputException when one of the parameters is incorrect.
     * @throws DecisionException upon failure.
     * @throws ClassFileNotFoundException if {@code valToLoad} is a symbolic 
     *         reference and 
     *         {@code valToLoad.}{@link Signature#getClassName() getClassName()}
     *         or the class name of one of its possibile expansions does 
     *         not have a classfile in the current state's classpath.
     * @throws ClassFileIllFormedException if {@code valToLoad} is a symbolic 
     *         reference and the classfile for
     *         {@code valToLoad.}{@link Signature#getClassName() getClassName()}
     *         or for the class name of one of its possibile expansions is ill-formed.
     * @throws BadClassFileVersionException when {@code valToLoad} is a symbolic 
     *         reference and the bytecode for
     *         {@code valToLoad.}{@link Signature#getClassName() getClassName()}
     *         or for the class name of one of its possibile expansions has a version
     *         number that is unsupported by this version of JBSE.
     * @throws RenameUnsupportedException when {@code valToLoad} is a symbolic 
     *         reference and the class
     *         {@code valToLoad.}{@link Signature#getClassName() getClassName()}
     *         or the class of one of its possibile expansions derives from a 
     *         model class but the classfile does not support renaming.
     * @throws WrongClassNameException when {@code valToLoad} is a symbolic 
     *         reference and the bytecode for
     *         {@code valToLoad.}{@link Signature#getClassName() getClassName()}
     *         or for the class name of one of its possibile expansions has a 
     *         class name different from the expected one ({@code valToLoad.}{@link Signature#getClassName() getClassName()}
     *         or the expansion's name).
     * @throws IncompatibleClassFileException if {@code valToLoad} is a symbolic 
     *         reference and the superclass of class
     *         {@code valToLoad.}{@link Signature#getClassName() getClassName()}
     *         is resolved to an interface class, or any superinterface is resolved
     *         to an object class.
     * @throws ClassFileNotAccessibleException if {@code valToLoad} is a symbolic 
     *         reference and the classfile for
     *         {@code valToLoad.}{@link Signature#getClassName() getClassName()}
     *         or for the class name of one of its possibile expansions cannot access
     *         one of its superclass/superinterfaces.
     * @throws ContradictionException in the case of guidance procedures during class 
     *         initialization of backdoor expansions.
     * @throws InterruptException in the case of guidance procedures during class 
     *         initialization of backdoor expansions.
     * @throws HeapMemoryExhaustedException in the case of guidance procedures during class 
     *         initialization of backdoor expansions.
     * @throws ClasspathException in the case of guidance procedures during class 
     *         initialization of backdoor expansions.
     */
    //TODO should be final?
    public Outcome resolve_XALOAD(List<ArrayAccessInfo> arrayAccessInfos, SortedSet<DecisionAlternative_XALOAD> result, List<ReferenceSymbolic> nonExpandedRefs)
    throws InvalidInputException, DecisionException, ClassFileNotFoundException, 
    ClassFileIllFormedException, BadClassFileVersionException, RenameUnsupportedException, 
    WrongClassNameException, IncompatibleClassFileException, ClassFileNotAccessibleException, ClasspathException, HeapMemoryExhaustedException, InterruptException, ContradictionException {
        if (arrayAccessInfos == null || result == null || nonExpandedRefs == null) {
            throw new InvalidInputException("resolve_XALOAD invoked with a null parameter.");
        }
        boolean someReferenceNotExpanded = false;
        boolean shouldRefine = false;
        boolean branchingDecision = false;
        for (ArrayAccessInfo arrayAccessInfo : arrayAccessInfos) {
            final boolean accessConcrete = (arrayAccessInfo.accessExpression == null);
            final boolean accessOutOfBounds = (arrayAccessInfo.readValue == null);
            final boolean valToLoadResolved = accessOutOfBounds || isResolved(getAssumptions(), arrayAccessInfo.readValue);
            final Outcome o;
            if (valToLoadResolved && accessConcrete) {
                o = resolve_XALOAD_ResolvedConcrete(arrayAccessInfo, result);
            } else if (valToLoadResolved && !accessConcrete) {
                o = resolve_XALOAD_ResolvedNonconcrete(arrayAccessInfo, result);
            } else { //(!valToLoadResolved)
                o = resolve_XALOAD_Unresolved(this.currentStateSupplier.get().getClassHierarchy(), arrayAccessInfo, result);
            }
            
            //if the current resolution did not expand a reference, then records it
            someReferenceNotExpanded = someReferenceNotExpanded || o.partialReferenceResolution();
            if (o.partialReferenceResolution()) {
                nonExpandedRefs.add((ReferenceSymbolic) arrayAccessInfo.readValue);
            }

            //if at least one read requires refinement, then it should be refined
            shouldRefine = shouldRefine || o.shouldRefine();

            //if at least one decision is branching, then it is branching
            branchingDecision = branchingDecision || o.branchingDecision();
        }

        //also the size of the result matters to whether refine or not 
        shouldRefine = shouldRefine || (result.size() > 1);
        
        //for branchingDecision nothing to do: it will be false only if
        //the access is concrete and the value obtained is resolved 
        //(if a symbolic reference): in this case, result.size() must
        //be 1. Note that branchingDecision must be invariant
        //on the used decision procedure, so we cannot make it dependent
        //on result.size().
        return Outcome.val(shouldRefine, someReferenceNotExpanded, branchingDecision);
    }

    /**
     * Resolves loading a value from an array to the operand stack, 
     * in the case the value to load is resolved (i.e., either 
     * concrete, or a symbolic primitive, or a resolved symbolic
     * reference) and the index used for the access is concrete.
     * 
     * @param arrayAccessInfo an {@link ArrayAccessInfo}.  
     * @param result a {@link SortedSet}{@code <}{@link DecisionAlternative_XALOAD}{@code >}, 
     *        where the method will put all the 
     *        {@link DecisionAlternative_XALOAD}s representing all the 
     *        satisfiable outcomes of the operation.
     * @return an {@link Outcome}.
     * @see {@link #resolve_XALOAD(List, SortedSet, List) resolve_XALOAD}.
     */
    private Outcome resolve_XALOAD_ResolvedConcrete(ArrayAccessInfo arrayAccessInfo, SortedSet<DecisionAlternative_XALOAD> result) {
        final boolean accessOutOfBounds = (arrayAccessInfo.readValue == null);
        final int branchNumber = result.size() + 1;
        if (accessOutOfBounds) {
            result.add(new DecisionAlternative_XALOAD_Out(branchNumber));
        } else {
            result.add(new DecisionAlternative_XALOAD_Resolved(arrayAccessInfo.readValue, arrayAccessInfo.fresh, arrayAccessInfo.sourceArrayReference, branchNumber));
        }
        return Outcome.val(arrayAccessInfo.fresh, false, false); //a fresh value to load requires refinement of the source array
    }

    /**
     * Resolves loading a value from an array to the operand stack, 
     * in the case the value to load is resolved (i.e., either 
     * concrete, or a symbolic primitive, or a resolved symbolic
     * reference) and the index used for the access is symbolic.
     * 
     * @param arrayAccessInfo an {@link ArrayAccessInfo}.
     * @param result a {@link SortedSet}{@code <}{@link DecisionAlternative_XALOAD}{@code >}, 
     *        where the method will put all the 
     *        {@link DecisionAlternative_XALOAD}s representing all the 
     *        satisfiable outcomes of the operation.
     * @return an {@link Outcome}.
     * @see {@link #resolve_XALOAD(List, SortedSet, List) resolve_XALOAD}.
     */
    protected Outcome resolve_XALOAD_ResolvedNonconcrete(ArrayAccessInfo arrayAccessInfo, SortedSet<DecisionAlternative_XALOAD> result)
    throws DecisionException {
        try {
            final boolean shouldRefine;
            final Primitive accessExpressionSpecialized = this.calc.push(arrayAccessInfo.accessExpression).replace(arrayAccessInfo.indexFormal, arrayAccessInfo.indexActual).pop();
            final boolean accessIsSat;
            if (accessExpressionSpecialized instanceof Simplex) {
            	accessIsSat = accessExpressionSpecialized.surelyTrue();
            } else {
            	accessIsSat = isSat((Expression) accessExpressionSpecialized);
            }
            if (accessIsSat) {
            	shouldRefine = arrayAccessInfo.fresh; //a fresh value to load requires refinement of the source array
            	final Primitive accessExpressionSimplified = deleteRedundantConjuncts(accessExpressionSpecialized);
            	final boolean accessOutOfBounds = (arrayAccessInfo.readValue == null);
            	final int branchNumber = result.size() + 1;
            	if (accessOutOfBounds) {
            		result.add(new DecisionAlternative_XALOAD_Out(arrayAccessInfo.accessExpression, arrayAccessInfo.indexFormal, arrayAccessInfo.indexActual, ((accessExpressionSimplified == null || accessExpressionSimplified.surelyTrue()) ? null : (Expression) accessExpressionSimplified), branchNumber));
            	} else {
            		result.add(new DecisionAlternative_XALOAD_Resolved(arrayAccessInfo.accessExpression, arrayAccessInfo.indexFormal, arrayAccessInfo.indexActual, ((accessExpressionSimplified == null || accessExpressionSimplified.surelyTrue()) ? null : (Expression) accessExpressionSimplified), arrayAccessInfo.readValue, arrayAccessInfo.fresh, arrayAccessInfo.sourceArrayReference, branchNumber));
            	}
            } else {
                //accessExpression is unsatisfiable: nothing to do
                shouldRefine = false;
            }
            return Outcome.val(shouldRefine, false, true);
        } catch (InvalidInputException | InvalidTypeException | InvalidOperandException e) {
            //this should never happen as arguments have been checked by the caller
            throw new UnexpectedInternalException(e);
        }
    }

    private Primitive deleteRedundantConjuncts(Primitive p) 
    throws DecisionException, InvalidInputException, InvalidTypeException, InvalidOperandException {
        if (p == null) {
            return null;
        }
        if (p instanceof Expression) {
            final Expression pExpr = (Expression) p;
            if (pExpr.getOperator() == Operator.AND) {
                final Primitive firstConjunctSimplified = deleteRedundantConjuncts(pExpr.getFirstOperand());
                final Primitive secondConjunctSimplified = deleteRedundantConjuncts(pExpr.getSecondOperand());
                return this.calc.push(firstConjunctSimplified).and(secondConjunctSimplified).pop();
            } else {
                final boolean subExpressionRedundant = !isSat((Expression) this.calc.push(pExpr).not().pop());
                if (subExpressionRedundant) {
                    return this.calc.valBoolean(true);
                } else {
                    return pExpr;
                }
            }
        } else {
            return p;
        }
    }

    /**
     * Resolves loading a value from an array to the operand stack, 
     * in the case the value to load is an unresolved symbolic
     * reference.
     * 
     * @param hier a {@link ClassHierarchy}. 
     * @param arrayAccessInfo an {@link ArrayAccessInfo}.
     * @param result a {@link SortedSet}{@code <}{@link DecisionAlternative_XALOAD}{@code >}, 
     *        where the method will put all the 
     *        {@link DecisionAlternative_XALOAD}s representing all the 
     *        satisfiable outcomes of the operation.
     * @return an {@link Outcome}.
     * @throws ClassFileNotFoundException when 
     *         {@code refToLoad.}{@link Signature#getClassName() getClassName()}
     *         or the class name of one of its possibile expansions does 
     *         not have a classfile in the classpath.
     * @throws ClassFileIllFormedException when the classfile for
     *         {@code refToLoad.}{@link Signature#getClassName() getClassName()}
     *         or for the class name of one of its possibile expansions is ill-formed.
     * @throws BadClassFileVersionException when the bytecode for
     *         {@code refToLoad.}{@link Signature#getClassName() getClassName()}
     *         or for the class name of one of its possibile expansions has a version
     *         number that is unsupported by this version of JBSE.
     * @throws RenameUnsupportedException if the classfile for 
     *         {@code refToLoad.}{@link Signature#getClassName() getClassName()}
     *         or for the class name of one of its possibile expansions derives from a 
     *         model class but the classfile does not support renaming.
     * @throws WrongClassNameException when the bytecode for
     *         {@code refToLoad.}{@link Signature#getClassName() getClassName()}
     *         or for the class name of one of its possibile expansions has a 
     *         class name different from the expected one ({@code refToLoad.}{@link Signature#getClassName() getClassName()}
     *         or the expansion's name).
     * @throws IncompatibleClassFileException when the superclass of class
     *         {@code refToLoad.}{@link Signature#getClassName() getClassName()}
     *         is resolved to an interface class, or any superinterface is resolved
     *         to an object class.
     * @throws ClassFileNotAccessibleException when the classfile for
     *         {@code refToLoad.}{@link Signature#getClassName() getClassName()}
     *         or for the class name of one of its possibile expansions cannot access
     *         one of its superclass/superinterfaces.
     * @throws ContradictionException in the case of guidance procedures during class 
     *         initialization of backdoor expansions.
     * @throws InterruptException in the case of guidance procedures during class 
     *         initialization of backdoor expansions.
     * @throws HeapMemoryExhaustedException in the case of guidance procedures during class 
     *         initialization of backdoor expansions.
     * @throws ClasspathException in the case of guidance procedures during class 
     *         initialization of backdoor expansions.
     * @see {@link #resolve_XALOAD(State, List, SortedSet) resolve_XALOAD}.
     */
    protected Outcome resolve_XALOAD_Unresolved(ClassHierarchy hier, ArrayAccessInfo arrayAccessInfo, SortedSet<DecisionAlternative_XALOAD> result)
    throws DecisionException, ClassFileNotFoundException, ClassFileIllFormedException, 
    BadClassFileVersionException, RenameUnsupportedException, WrongClassNameException, 
    IncompatibleClassFileException, ClassFileNotAccessibleException, ClasspathException, HeapMemoryExhaustedException, InterruptException, ContradictionException {
        try {
            final boolean accessConcrete = (arrayAccessInfo.accessExpression == null);
            final Primitive accessExpressionSpecialized = (accessConcrete ? null : this.calc.push(arrayAccessInfo.accessExpression).replace(arrayAccessInfo.indexFormal, arrayAccessInfo.indexActual).pop());
            final boolean accessIsSat;
            if (accessExpressionSpecialized instanceof Simplex) {
            	accessIsSat = accessExpressionSpecialized.surelyTrue();
            } else {
            	accessIsSat = accessConcrete || isSat((Expression) accessExpressionSpecialized);
            }
            final boolean shouldRefine;
            final boolean noReferenceExpansion;
            if (accessIsSat) {
                shouldRefine = true; //unresolved symbolic references always require a refinement action
                final Primitive accessExpressionSimplified = deleteRedundantConjuncts(accessExpressionSpecialized);
                noReferenceExpansion =
                    doResolveReference(hier, (ReferenceSymbolic) arrayAccessInfo.readValue, new DecisionAlternativeReferenceFactory_XALOAD(arrayAccessInfo.accessExpression, arrayAccessInfo.indexFormal, arrayAccessInfo.indexActual, ((accessExpressionSimplified == null || accessExpressionSimplified.surelyTrue()) ? null : (Expression) accessExpressionSimplified), arrayAccessInfo.fresh, arrayAccessInfo.sourceArrayReference), result);
            } else {
                //accessExpression is unsatisfiable: nothing to do
                shouldRefine = false;
                noReferenceExpansion = false;
            }
            return Outcome.val(shouldRefine, noReferenceExpansion, true);
        } catch (InvalidInputException | InvalidTypeException | InvalidOperandException e) {
            //this should never happen as arguments have been checked by the caller
            throw new UnexpectedInternalException(e);
        }
    }

    /**
     * Resolution of unresolved references.
     * 
     * @param <D> The class for the decision alternatives.
     * @param <DA> The class for the decision alternative for 
     *            resolutions by aliasing. Must be a subclass of {@code <D>}.
     * @param <DE> The class for the decision alternative for
     *            resolutions by expansion. Must be a subclass of {@code <D>}.
     * @param <DN> The class for the decision alternative for
     *            resolutions by null. Must be a subclass of {@code <D>}.
     * @param hier a {@link ClassHierarchy}.
     * @param refToResolve the {@link ReferenceSymbolic} to resolve.
     * @param factory A Concrete Factory for decision alternatives.
     * @param result a {@link SortedSet}{@code <D>}, which the method 
     *            will update by adding to it all the decision alternatives 
     *            representing all the valid expansions of {@code notInitializedRef}.
     * @return {@code true} iff the resolution of the reference is 
     *         partial (see {@link Outcome#partialReferenceResolution()}).
     * @throws InvalidInputException when one of the parameters is incorrect.
     * @throws DecisionException upon failure.
     * @throws ClassFileNotFoundException when 
     *         {@code refToResolve.}{@link Signature#getClassName() getClassName()}
     *         or the class name of one of its possibile expansions does 
     *         not have a classfile in the classpath.
     * @throws ClassFileIllFormedException when the bytecode for
     *         {@code refToResolve.}{@link Signature#getClassName() getClassName()}
     *         or for the class name of one of its possibile expansions is ill-formed.
     * @throws BadClassFileVersionException when the bytecode for
     *         {@code refToResolve.}{@link Signature#getClassName() getClassName()}
     *         or for the class name of one of its possibile expansions has a version
     *         number that is unsupported by this version of JBSE.
     * @throws RenameUnsupportedException if {@code refToResolve.}{@link Signature#getClassName() getClassName()}
     *         or the class of one of its possibile expansions derives from a 
     *         model class but the classfile does not support renaming.
     * @throws WrongClassNameException when the bytecode for
     *         {@code refToResolve.}{@link Signature#getClassName() getClassName()}
     *         or for the class name of one of its possibile expansions has a 
     *         class name different from the expected one ({@code refToResolve.}{@link Signature#getClassName() getClassName()}
     *         or the expansion's name).
     * @throws IncompatibleClassFileException if the superclass of {@code refToResolve.}{@link Signature#getClassName() getClassName()}
     *         or for one of its possibile expansions is resolved to an interface 
     *         class, or any superinterface is resolved to an object class.
     * @throws ClassFileNotAccessibleException when the classfile for
     *         {@code refToResolve.}{@link Signature#getClassName() getClassName()}
     *         or for the class name of one of its possibile expansions cannot access
     *         one of its superclass/superinterfaces.
     */
    protected <D, DA extends D, DE extends D, DN extends D> 
    boolean doResolveReference(ClassHierarchy hier, ReferenceSymbolic refToResolve, 
    DecisionAlternativeReferenceFactory<DA, DE, DN> factory, SortedSet<D> result) 
    throws InvalidInputException, DecisionException, ClassFileNotFoundException, 
    ClassFileIllFormedException, BadClassFileVersionException, RenameUnsupportedException, 
    WrongClassNameException, IncompatibleClassFileException, ClassFileNotAccessibleException {
        int branchCounter = result.size() + 1;

        //loads the most precise subclass of the reference's static type 
        //that can be inferred from the generic type information, and determines
        //whether it can be assumed to be initialized (if not, 
        //the only compatible resolution of the reference is null)
        //TODO loading the class of the reference, invoking getPossibleAliases and getPossibleExpansions introduces unwanted dependence on State
        final ClassFile refToResolveClass;
        try {
            refToResolveClass = hier.loadCreateClass(CLASSLOADER_APP, mostPreciseResolutionClassName(hier, refToResolve), true);  
            //TODO instead of rethrowing exceptions (class file not found, ill-formed or unaccessible) just set refToResolveTypeIsSatInitialized to false??
        } catch (PleaseLoadClassException e) {
            //this should never happen since we bypassed standard loading
            throw new UnexpectedInternalException(e);
        }
        final boolean refToResolveTypeIsSatInitialized = isSatInitialized(refToResolveClass);

        boolean partialReferenceResolution = true;
        if (refToResolveTypeIsSatInitialized) {
            //filters static aliases based on their satisfiability
            final Map<Long, Objekt> possibleAliases = getPossibleAliases(refToResolve, refToResolveClass);
            if (possibleAliases == null) {
                throw new UnexpectedInternalException("Symbolic reference " + refToResolve.toString() + 
                                                      " (" + refToResolve.asOriginString() + ") has a bad type " + refToResolve.getStaticType() + ".");
            }
            for (Map.Entry<Long, Objekt> ae : possibleAliases.entrySet()) {
                final long i = ae.getKey();
                final Objekt o = ae.getValue();
                if (isSatAliases(refToResolve, i, o)) {
                    final DA a = factory.createAlternativeRefAliases(refToResolve, i, o.getOrigin(), branchCounter);
                    result.add(a);
                }
                ++branchCounter;
            }

            //same for static expansions
            final Set<ClassFile> possibleExpansions = getPossibleExpansions(hier, refToResolveClass);
            if (possibleExpansions == null) {
                throw new UnexpectedInternalException("Symbolic reference " + refToResolve + 
                                                      " (" + refToResolve.asOriginString() + ") has a bad type " + refToResolve.getStaticType() + ".");
            }
            for (ClassFile expansionClass : possibleExpansions) {
                if (isSatInitialized(expansionClass)) {
                    partialReferenceResolution = false;
                	if (isSatExpands(refToResolve, expansionClass)) {
                        final DE e = factory.createAlternativeRefExpands(refToResolve, expansionClass, branchCounter);
                        result.add(e);
                    }
                }
                ++branchCounter;
            }
        }

        //same for null
        if (isSatNull(refToResolve)) {
            final DN n = factory.createAlternativeRefNull(refToResolve, branchCounter);
            result.add(n);
            //no need to increment branchNumber
        }

        //is there a partial reference resolution?
        return partialReferenceResolution;
    }
    
    protected final String mostPreciseResolutionClassName(ClassHierarchy hier, ReferenceSymbolic refToResolve) 
    throws InvalidInputException, ClassFileNotFoundException, ClassFileIllFormedException, ClassFileNotAccessibleException, 
    IncompatibleClassFileException, PleaseLoadClassException, BadClassFileVersionException, RenameUnsupportedException, 
    WrongClassNameException {
    	final String staticClassName = className(refToResolve.getStaticType());
    	final String mostPreciseResolutionClassName;
    	if (refToResolve instanceof ReferenceSymbolicAtomic) {
    		final ReferenceSymbolicAtomic refToResolveAtomic = (ReferenceSymbolicAtomic) refToResolve;
    		final String genericSignatureType = refToResolveAtomic.getGenericSignatureType();
    		if (staticClassName.equals(className(eraseGenericParameters(genericSignatureType)))) {
    			//the generic type does not convey any additional information
        		mostPreciseResolutionClassName = staticClassName;
    		} else if (isTypeParameter(genericSignatureType)) {
    			mostPreciseResolutionClassName = solveTypeInformation(hier, refToResolveAtomic, staticClassName, typeParameterIdentifier(genericSignatureType));
    		} else {
    			//this should not happen, but in any case there is no
    			//relevant information that can be exploited
        		mostPreciseResolutionClassName = staticClassName;
    		}
    	} else {
    		//the reference symbolic is the result of an uninterpreted function
    		//application: no interesting information here
    		//TODO really?
    		mostPreciseResolutionClassName = staticClassName;
    	}
    	return mostPreciseResolutionClassName;
    }
    
    private String solveTypeInformation(ClassHierarchy hier, ReferenceSymbolic refToResolve, String staticClassName, String typeParameter) 
    throws ClassFileIllFormedException, InvalidInputException, ClassFileNotFoundException, ClassFileNotAccessibleException, IncompatibleClassFileException, 
    PleaseLoadClassException, BadClassFileVersionException, RenameUnsupportedException, WrongClassNameException {
    	final SolverEquationGenericTypes solver = new SolverEquationGenericTypes();
    	ReferenceSymbolic ref = refToResolve;
    	while (ref instanceof ReferenceSymbolicMember) {
        	//gets the container reference, where the type parameter is 
        	//instantiated, and its class, where the type parameter is declared
        	final ReferenceSymbolicMember refMember = (ReferenceSymbolicMember) ref;
    		final ReferenceSymbolic containerRef;
    		{
    			final ReferenceSymbolic containerRefPre = refMember.getContainer();
    			if (containerRefPre instanceof KlassPseudoReference) {
    				break;
    			} else if (containerRefPre instanceof ReferenceSymbolicMemberArray || containerRefPre instanceof ReferenceSymbolicMemberMapValue) {
    				containerRef = ((ReferenceSymbolicMember) containerRefPre).getContainer();
    			} else {
    				containerRef = containerRefPre;
    			}
    		}
        	final ClassFile containerRefClass;
    		try {
    			containerRefClass = hier.loadCreateClass(CLASSLOADER_APP, className(containerRef.getStaticType()), true);
    		} catch (InvalidInputException | ClassFileNotFoundException | 
    				ClassFileIllFormedException | ClassFileNotAccessibleException | IncompatibleClassFileException | 
    				PleaseLoadClassException | BadClassFileVersionException | WrongClassNameException e) {
    			//this should never happen
    			throw new UnexpectedInternalException(e);
    		}
    		
    		//finds the generic parameter declarations across
    		//the (possible) nesting of classes
    		ClassFile declClass = containerRefClass;
    		final LinkedList<String> containerTypeInstantiationsList = new LinkedList<>();
    		while (true) {
    			final String classSignatureType = declClass.getGenericSignatureType();
        		final String classTypeParameters;
    			if (classSignatureType == null) {
    				classTypeParameters = "";
    			} else {
    				classTypeParameters = splitClassGenericSignatureTypeParameters(classSignatureType);
    			}
    			containerTypeInstantiationsList.addFirst(REFERENCE + declClass.getClassName() + classTypeParameters + TYPEEND);
    			final String next = declClass.classContainer();
    			if (next == null) {
    				break;
    			}
    			declClass = hier.loadCreateClass(CLASSLOADER_APP, next, true);
    		}
    		
    		//analyzes the container reference to determine the 
    		//instantiation of the generic parameter
    		final String containerSignatureType;
    		if (containerRef instanceof ReferenceSymbolicAtomic) {
    			final ReferenceSymbolicAtomic containerAtomic = (ReferenceSymbolicAtomic) containerRef;
    			containerSignatureType = containerAtomic.getGenericSignatureType();
    		} else {
    			break; //nothing else to do
    		}
    		final String[] containerSignatureTypes = containerSignatureType.split("\\.");
    		for (int i = 1; i < containerSignatureTypes.length; ++i) {
        	    	containerSignatureTypes[i - 1] = containerSignatureTypes[i - 1] + TYPEEND;
        	    	final int start = splitClassGenericSignatureTypeParameters(containerSignatureTypes[i - 1]).length();
        	    	final String s = containerSignatureTypes[i - 1].substring(start);
        	    	final int end = s.indexOf('<');
        	    	containerSignatureTypes[i] = (end == -1 ? s : s.substring(0, end)) + "$" + containerSignatureTypes[i];
    		}
    		
    		//adds the equations
    		for (int k = 0; k < containerSignatureTypes.length; ++k) {
    			solver.addEquation(textToApply(containerRef.asOriginString(), containerTypeInstantiationsList.get(k)), textToTerm((containerRef instanceof ReferenceSymbolicMember ? ((ReferenceSymbolicMember) containerRef).getContainer().asOriginString() : ""), containerSignatureTypes[k]));
    		}
    		
    		ref = containerRef;
    	}
    	
    	solver.solve();
    	if (solver.hasSolution()) {
    		final String solution = solver.getVariableValue(new Var((refToResolve instanceof ReferenceSymbolicMember ? ((ReferenceSymbolicMember) refToResolve).getContainer().asOriginString() : "") + "?" + typeParameter));
    		return (solution == null ? staticClassName : solution); //solution == null when no equations, i.e., when refToResolve is not a ReferenceSymbolicMember
    	} else {
    		return staticClassName;
    	}
    }
    
	private static final Var[] EMPTY_VAR_ARRAY = new Var[0];
    
	private static Apply textToApply(String prefix, String text) {
	    final int langleIndex = text.indexOf('<');
	    final String functor = (langleIndex == -1 ? text.substring(1, text.length() - 1) : text.substring(1, langleIndex));
	    final String[] preVars = (langleIndex == -1 ? new String[0] : text.substring(langleIndex + 1).split(":"));
            final ArrayList<Var> vars = new ArrayList<>();
	    for (int k = 0; k < preVars.length - 1; ++k) {
	        vars.add(new Var(prefix + "?" + (k == 0 ? preVars[k] : preVars[k].substring(preVars[k].indexOf(';') + 1))));
	    }

	    return new Apply(functor, vars.toArray(EMPTY_VAR_ARRAY));
	}

	private static final TypeTerm[] EMPTY_TYPETERM_ARRAY = new TypeTerm[0];
	
	private static TypeTerm textToTerm(String prefix, String text) throws InvalidInputException {
		if (text.charAt(0) == TYPEVAR) {
			return new Var(prefix + "?" + text.substring(1, text.length() - 1));
		} else if (text.equals("*")) {
			return Some.instance();
		} else if (text.charAt(0) == REFERENCE || text.charAt(0) == ARRAYOF) {
			final int langleIndex = text.indexOf('<');
			final String functor = text.substring(1, (langleIndex == -1 ? (text.length() - 1) : langleIndex));
			if (langleIndex == -1) {
				return new Apply(functor);
			} else {
				final ArrayList<TypeTerm> args = new ArrayList<>();
				int i = functor.length() + 2;
				boolean unknown = false;
				while (i < text.length()) {
					final char c = text.charAt(i);
					if (c == '*') {
						args.add(textToTerm(prefix, text.substring(i, i + 1)));
						++i;
					} else if (c == '+' || c == '-') {
						unknown = true;
						++i;
					} else if (c == TYPEVAR) {
						final int start = i;
						do {
							++i;
						} while (text.charAt(i) != TYPEEND);
						args.add(unknown ? Some.instance() : textToTerm(prefix, text.substring(start, i + 1)));
						++i;
						unknown = false;
					} else if (c == REFERENCE) {
						final int start = i;
						int level = 1;
						do {
							++i;
							if (text.charAt(i) == '<') {
								++level;
							} else if (text.charAt(i) == '>') {
								--level;
							}
						} while (text.charAt(i) != TYPEEND || level != 1);
						args.add(unknown ? Some.instance() : textToTerm(prefix, text.substring(start, i + 1)));
						++i;
						unknown = false;
					} else if (c == ARRAYOF) {
						final int start = i;
						do {
							++i;
						} while (text.charAt(i) == ARRAYOF);
						if (text.charAt(i) == TYPEVAR || text.charAt(i) == REFERENCE) {
							int level = 1;
							do {
								++i;
								if (text.charAt(i) == '<') {
									++level;
								} else if (text.charAt(i) == '>') {
									--level;
								}
							} while (text.charAt(i) != TYPEEND || level != 1);
						}
						args.add(unknown ? Some.instance() : textToTerm(prefix, text.substring(start, i + 1)));
						++i;
						unknown = false;
					} else if (c == '>') {
						break;
					} else {
						throw new InvalidInputException("Cannot parse as a generic type equational term the string: " + text + ".");
					}
				}
				return new Apply(functor, args.toArray(EMPTY_TYPETERM_ARRAY));
			}
		} else {
			throw new InvalidInputException("Cannot parse as a generic type equational term the string: " + text + ".");
		}
	}

	/**
     * Returns all the possible aliases of a given 
     * {@link ReferenceSymbolic}.
     *
     * @param ref a {@link ReferenceSymbolic} to be resolved.
     * @param refClass the {@link ClassFile} for the static type of {@code ref}.
     * @return a {@link Map}{@code <}{@link Long}{@code, }{@link Objekt}{@code >}, 
     *         representing a subview of the state's heap that contains
     *         all the objects that are compatible, in their type and epoch, with {@code ref}.
     *         If {@code ref} does not denote a reference or array type, the method 
     *         returns {@code null}.
	 * @throws DecisionException upon failure of getting the current assumptions.
     */
    private Map<Long, Objekt> getPossibleAliases(ReferenceSymbolic ref, ClassFile refClass) throws DecisionException {
        //checks preconditions
        if (!refClass.isReference() && !refClass.isArray()) {
            return null;
        }

        final TreeMap<Long, Objekt> retVal = new TreeMap<>();

        //TODO extract this code and share with State.getObjectInitial and jbse.rule.Util.getTriggerMethodParameterObject
        //scans the path condition for compatible objects
        final List<Clause> pathCondition = getAssumptions();
        for (Clause c : pathCondition) {
            if (c instanceof ClauseAssumeExpands) {
                //gets the object and its position in the heap
                final ClauseAssumeExpands cExp = (ClauseAssumeExpands) c;
                final Long i = cExp.getHeapPosition();
                final Objekt o = cExp.getObjekt();

                //if it is type and epoch compatible, adds the object
                //to the result
                try {
                    if (isAliasCompatible(o, ref, refClass)) {
                        retVal.put(i, o);
                    }
                } catch (InvalidInputException e) {
                    //this should never happen (checked before)
                    throw new UnexpectedInternalException(e);
                }
            }
        }
        return retVal;
    }

    /**
     * Checks whether an {@link Objekt} can be used 
     * to resolve of a symbolic reference by aliasing.
     * 
     * @param o an {@link Objekt}.
     * @param ref a {@link ReferenceSymbolic} to be resolved.
     * @param refClass the {@link ClassFile} for the static type of {@code ref}.
     * @return {@code true} iff {@code refClass} can be resolved by alias to 
     *         {@code o}. 
     *         More precisely, returns {@code true} iff the creation epoch of 
     *         {@code o} comes before that of the symbolic reference, and {@code o}'s type 
     *         is a subclass of {@code refClass}. If {@code ref} is a member of a 
     *         {@link ReferenceSymbolicApply} it also check that {@code o} has as
     *         origin the same {@link ReferenceSymbolicApply}.
     * @throws InvalidInputException if {@code refClass == null}.
     */
    private boolean isAliasCompatible(Objekt o, ReferenceSymbolic ref, ClassFile refClass) throws InvalidInputException {
        final boolean isTypeCompatible = o.getType().isSubclass(refClass);
        final HistoryPoint oEpoch = o.historyPoint();
        final HistoryPoint refEpoch = ref.historyPoint();
        final boolean isEpochCompatible = oEpoch.weaklyBefore(refEpoch);
        final boolean isRootCompatible = !(ref.root() instanceof ReferenceSymbolicApply) || o.getOrigin().root().equals(ref.root());
        return (isTypeCompatible && isEpochCompatible && isRootCompatible); 
    }

    /**
     * Returns all the heap objects in a state that may be possible
     * aliases of a given {@link ReferenceSymbolic}.
     *
     * @param hier a {@link ClassHierarchy}.
     * @param refClass a {@link ClassFile} for the static type of the reference 
     *        to be resolved.
     * @return a {@link Set}{@code <}{@link String}{@code >}, listing
     *         all the classes that are compatible, in their type and epoch of 
     *         initialization, with {@code ref}.
     *         If {@code ref} does not denote a reference or array type, the method 
     *         returns {@code null}.
     * @throws InvalidInputException if one of the candidate subclass names 
     *         for {@code refClass} in {@code state.}{@link State#getClassHierarchy() getClassHierarchy()}'s
     *         expansion backdoor is invalid.
     * @throws ClassFileNotFoundException if any of the candidate subclass names 
     *         for {@code refClass} in {@code state.}{@link State#getClassHierarchy() getClassHierarchy()}'s
     *         expansion backdoor does not have a classfile neither 
     *         in the bootstrap, nor in the 
     *         extension, nor in the application classpath.
     *         names does not denote a classfile in the classpath.
     * @throws ClassFileIllFormedException if any of the candidate subclass names 
     *         for {@code refClass} in {@code state.}{@link State#getClassHierarchy() getClassHierarchy()}'s
     *         expansion backdoor has an ill-formed bytecode.
     * @throws BadClassFileVersionException when the bytecode for any of the 
     *         candidate subclass names for {@code refClass} in 
     *         {@code state.}{@link State#getClassHierarchy() getClassHierarchy()}'s
     *         expansion backdoor has a version number that is unsupported by this version of JBSE.
     * @throws RenameUnsupportedException when any of the 
     *         candidate subclass names for {@code refClass} in 
     *         {@code state.}{@link State#getClassHierarchy() getClassHierarchy()}'s
     *         expansion backdoor derives from a 
     *         model class but the classfile does not support renaming.
     * @throws WrongClassNameException when the bytecode for any of the 
     *         candidate subclass names for {@code refClass} in 
     *         {@code state.}{@link State#getClassHierarchy() getClassHierarchy()}'s
     *         expansion backdoor has a class name different from the expected one 
     *         (the candidate subclass name).
     * @throws IncompatibleClassFileException if the superclass of any of the candidate subclasses 
     *         for {@code refClass} in {@code state.}{@link State#getClassHierarchy() getClassHierarchy()}'s
     *         expansion backdoor is resolved to an interface class, or any superinterface is resolved
     *         to an object class.
     * @throws ClassFileNotAccessibleException if the classfile of any of the candidate subclass names 
     *         for {@code refClass} in {@code state.}{@link State#getClassHierarchy() getClassHierarchy()}'s
     *         expansion backdoor cannot access one of its superclasses/superinterfaces.
     */
    private static Set<ClassFile> getPossibleExpansions(ClassHierarchy hier, ClassFile refClass) 
    throws InvalidInputException, ClassFileNotFoundException, ClassFileIllFormedException, 
    BadClassFileVersionException, RenameUnsupportedException, WrongClassNameException, 
    IncompatibleClassFileException, ClassFileNotAccessibleException {
        if (!refClass.isReference() && !refClass.isArray()) {
            return null;
        }

        final Set<ClassFile> retVal;
        if (refClass.isArray()) {
            //the (really trivial) array case:
            //array classes are final, concrete, and can
            //always be assumed to be initialized, so
            //this is the only expansion possible
            retVal = new HashSet<>();
            retVal.add(refClass);
        } else {
            retVal = hier.getAllConcreteSubclasses(refClass);
        }

        return retVal;
    }

    /**
     * Completes the set operation of an {@link Array} by constraining the affected entries
     * and removing the unsatisfiable ones.
     * 
     * @param hier a {@link ClassHierarchy}.
     * @param entries an {@link Iterator}{@code <? extends }{@link AccessOutcomeIn}{@code >}. The method
     *        will determine the entries affected by the set operation, constrain them, and 
     *        delete the entries that become unsatisfiable.
     * @param index a {@link Primitive}, the position in the {@link Array} which is set.
     * @throws InvalidInputException when one of the parameters is incorrect.
     * @throws DecisionException upon failure.
     */
    public void constrainArrayForSet(ClassHierarchy hier, Iterator<? extends Array.AccessOutcomeIn> entries, Primitive index) 
    throws InvalidInputException, DecisionException {
        if (hier == null || entries == null || index == null) {
            throw new InvalidInputException("completeArraySet invoked with a null parameter.");
        }
        if (index.getType() != Type.INT) {
            throw new InvalidInputException("completeArraySet invoked with an index having type " + index.getType());
        }
        try {
            while (entries.hasNext()) {
                final Array.AccessOutcomeIn e = entries.next();
                final Primitive indexInRange = e.inRange(this.calc, index);
                final boolean entryAffected;
                if (indexInRange instanceof Simplex) {
                    entryAffected = indexInRange.surelyTrue();
                } else {
                    entryAffected = isSat((Expression) indexInRange);
                }

                //if the entry is affected, it is constrained and possibly removed
                if (entryAffected) {
                    e.excludeIndexFromAccessCondition(this.calc, index);
                    final Expression accessCondition = e.getAccessCondition();
                    if (isSat(accessCondition)) {
                        //do nothing
                    } else {
                        entries.remove();
                    }
                }

            }
        } catch (InvalidTypeException exc) {
            //this should never happen after argument check
            throw new UnexpectedInternalException(exc);
        }
        //TODO coalesce entries that have same value (after investigating the impact on guided execution)
    }

    /**
     * Completes a {@code java.System.arraycopy} by 
     * constraining the affected entries and removing 
     * the unsatisfiable ones.
     * 
     * @param entries an {@link Iterator}{@code <? extends }{@link AccessOutcomeIn}{@code >}. The method
     *        will determine the entries affected by the copy operation, constrain them, and 
     *        delete the entries that become unsatisfiable.
     * @param srcPos The source initial position.
     * @param destPos The destination initial position.
     * @param length How many elements should be copied.
     * @throws InvalidInputException when one of the parameters is incorrect.
     * @throws DecisionException upon failure.
     */
    public void completeArraycopy(Iterator<? extends Array.AccessOutcomeIn> entries, Primitive srcPos, Primitive destPos, Primitive length) 
    throws InvalidInputException, DecisionException {
        if (entries == null || srcPos == null || destPos == null || length == null) {
            throw new InvalidInputException("completeArraycopy invoked with a null parameter.");
        }
        if (srcPos.getType() != Type.INT || destPos.getType() != Type.INT || length.getType() != Type.INT) {
            throw new InvalidInputException("completeArraycopy invoked with a nonint srcPos, destPos or length parameter.");
        }
        while (entries.hasNext()) {
            final Array.AccessOutcomeIn e = entries.next();
            final Expression accessCondition = e.getAccessCondition();
            if (isSat(accessCondition)) {
                //do nothing
            } else {
                entries.remove();
            }
        }
        //TODO coalesce entries that have same value (after investigating the impact on guided execution)
    }

    /**
     * Returns the only decision alternative for the expansion
     * of the {ROOT}:this reference.
     * 
     * @param rootThis a {@link ReferenceSymbolic}, the {ROOT}:this 
     *        reference.
     * @param classFile the {@link ClassFile} of the root object.
     * @return a {@link DecisionAlternative_XLOAD_GETX_Expands}.
     */
    public DecisionAlternative_XLOAD_GETX_Expands getRootDecisionAlternative(ReferenceSymbolic rootThis, ClassFile classFile) {
        return new DecisionAlternative_XLOAD_GETX_Expands(rootThis, classFile, 1);
    }
}