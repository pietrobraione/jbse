package jbse.dec;

import static jbse.bc.ClassLoaders.CLASSLOADER_APP;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.className;
import static jbse.common.Type.eraseGenericParameters;
import static jbse.common.Type.getDeclaredNumberOfDimensions;
import static jbse.common.Type.isArray;
import static jbse.common.Type.isTypeParameter;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.splitClassGenericSignatureTypeParameters;
import static jbse.common.Type.splitReturnValueDescriptor;
import static jbse.common.Type.TYPEEND;
import static jbse.common.Type.TYPEVAR;
import static jbse.common.Type.typeParameterIdentifier;
import static jbse.mem.Util.forAllInitialObjects;
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
import java.util.TreeSet;
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
import jbse.tree.DecisionAlternativeReferenceFactory;
import jbse.tree.DecisionAlternativeReferenceFactory_XALOAD;
import jbse.tree.DecisionAlternativeReferenceFactory_XLOAD_GETX;
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
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.ReferenceSymbolic;
import jbse.val.ReferenceSymbolicApply;
import jbse.val.ReferenceSymbolicMember;
import jbse.val.ReferenceSymbolicMemberArray;
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
     * @param partiallyResolvedReferences a {@link List}{@code <}{@link ReferenceSymbolic}{@code >} 
     *        that this method will populate with the {@link ReferenceSymbolic}s that 
     *        were partially resolved. It must not be {@code null}.
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
    public Outcome resolve_XALOAD(List<ArrayAccessInfo> arrayAccessInfos, SortedSet<DecisionAlternative_XALOAD> result, List<ReferenceSymbolic> partiallyResolvedReferences)
    throws InvalidInputException, DecisionException, ClassFileNotFoundException, 
    ClassFileIllFormedException, BadClassFileVersionException, RenameUnsupportedException, 
    WrongClassNameException, IncompatibleClassFileException, ClassFileNotAccessibleException, ClasspathException, HeapMemoryExhaustedException, InterruptException, ContradictionException {
        if (arrayAccessInfos == null || result == null || partiallyResolvedReferences == null) {
            throw new InvalidInputException("resolve_XALOAD invoked with a null parameter.");
        }
        boolean partialReferenceResolution = false;
        boolean shouldRefine = false;
        boolean branchingDecision = false;
        for (ArrayAccessInfo arrayAccessInfo : arrayAccessInfos) {
            final boolean accessConcrete = (arrayAccessInfo.accessExpression == null);
            final boolean accessOutOfBounds = (arrayAccessInfo.readValue == null);
            final boolean valToLoadResolved = accessOutOfBounds || isResolved(getAssumptions(), arrayAccessInfo.readValue);
            final Outcome o;
            final TreeSet<DecisionAlternative_XALOAD> localResult = new TreeSet<>(result.comparator());
            if (valToLoadResolved && accessConcrete) {
                o = resolve_XALOAD_ResolvedConcrete(arrayAccessInfo, localResult);
            } else if (valToLoadResolved && !accessConcrete) {
                o = resolve_XALOAD_ResolvedNonconcrete(arrayAccessInfo, localResult);
            } else { //(!valToLoadResolved)
                o = resolve_XALOAD_Unresolved(this.currentStateSupplier.get().getClassHierarchy(), arrayAccessInfo, localResult);
            }
            result.addAll(localResult);
            
            //if the current resolution was partial, then records it
            partialReferenceResolution = partialReferenceResolution || o.partialReferenceResolution();
            if (o.partialReferenceResolution()) {
                partiallyResolvedReferences.add((ReferenceSymbolic) arrayAccessInfo.readValue);
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
        return Outcome.val(shouldRefine, partialReferenceResolution, branchingDecision);
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
            
            //it is also necessary to load the class of the static type of the
            //reference to resolve, because in case the most precise resolution
            //class differs from it, it is possible that this is never loaded
            hier.loadCreateClass(CLASSLOADER_APP, className(refToResolve.getStaticType()), true);
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
    
    /**
     * Determines the most precise resolution class of a symbolic reference, 
     * based on the information of the generic signatures of variables/fields/classes.
     * 
     * @param hier a {@link ClassHierarchy}. It must not be {@code null}.
     * @param refToResolve the {@link ReferenceSymbolic} to resolve. It must not be {@code null}.
     * @return a {@link String}, the most precise resolution class name for {@code refToResolve}.
     * @throws InvalidInputException if {@code hier == null || refToResolve == null}.
     * @throws ClassFileNotFoundException if {@code hier} fails to load the erasure of the signature type
     *         of {@code refToResolve} or of its containers, and throws {@link ClassFileNotFoundException}.
     * @throws ClassFileIllFormedException if {@code hier} fails to load the erasure of the signature type
     *         of {@code refToResolve} or of its containers, and throws {@link ClassFileIllFormedException}.
     * @throws ClassFileNotAccessibleException if {@code hier} fails to load the erasure of the signature type
     *         of {@code refToResolve} or of its containers, and throws {@link ClassFileNotAccessibleException}.
     * @throws IncompatibleClassFileException if {@code hier} fails to load the erasure of the signature type
     *         of {@code refToResolve} or of its containers, and throws {@link IncompatibleClassFileException}.
     * @throws BadClassFileVersionException if {@code hier} fails to load the erasure of the signature type
     *         of {@code refToResolve} or of its containers, and throws {@link BadClassFileVersionException}.
     * @throws RenameUnsupportedException if {@code hier} fails to load the erasure of the signature type
     *         of {@code refToResolve} or of its containers, and throws {@link RenameUnsupportedException}.
     * @throws WrongClassNameException if {@code hier} fails to load the erasure of the signature type
     *         of {@code refToResolve} or of its containers, and throws {@link WrongClassNameException}.
     * @throws DecisionException if this decision procedure fails.
     */
    protected final String mostPreciseResolutionClassName(ClassHierarchy hier, ReferenceSymbolic refToResolve) 
    throws InvalidInputException, ClassFileNotFoundException, ClassFileIllFormedException, ClassFileNotAccessibleException, 
    IncompatibleClassFileException, BadClassFileVersionException, RenameUnsupportedException, 
    WrongClassNameException, DecisionException {
    	if (hier == null || refToResolve == null) {
    		throw new InvalidInputException("DecisionProcedureAlgorithm.mostPreciseResolutionClassName: Invoked with a null parameter.");
    	}
    	
    	try {
    		final String staticClassName = className(refToResolve.getStaticType());
    		final String mostPreciseResolutionClassName;
    		final String genericSignatureType = refToResolve.getGenericSignatureType();
    		if (staticClassName.equals(className(splitReturnValueDescriptor(eraseGenericParameters(genericSignatureType))))) {
    			//the generic type does not convey any additional information than the
    			//static type
    			mostPreciseResolutionClassName = staticClassName;
    		} else {
    			//determines the minimal generic (nonarray) container containing 
    			//refToResolve
    			ReferenceSymbolic refToResolveContainer = refToResolve;
    			while (refToResolveContainer instanceof ReferenceSymbolicMemberArray) {
    				refToResolveContainer = ((ReferenceSymbolicMemberArray) refToResolveContainer).getContainer();
    			}
    			if (refToResolveContainer instanceof ReferenceSymbolicMember) {
    				refToResolveContainer = ((ReferenceSymbolicMember) refToResolveContainer).getContainer();

    				//determines the generic type parameter unknown
    				final int uplevel = (isArray(genericSignatureType) ? getDeclaredNumberOfDimensions(genericSignatureType) : 0);
    				final String typeParameterUnknown = typeParameterIdentifier(genericSignatureType.substring(uplevel));

    				//solves
    				final String solution = solveTypeInformation(hier, refToResolveContainer, typeParameterUnknown);

    				//finally, determines the true result
    				if (solution == null) {
    					mostPreciseResolutionClassName = staticClassName;
    				} else if (isArray(genericSignatureType)) {
    					final StringBuilder sb = new StringBuilder();
    					for (int i = 1; i <= uplevel; ++i) {
    						sb.append(ARRAYOF);
    					}
    					sb.append(REFERENCE);
    					sb.append(solution);
    					sb.append(TYPEEND);
    					mostPreciseResolutionClassName = sb.toString();
    				} else {
    					mostPreciseResolutionClassName = solution;
    				}
    			} else {
    				//refToResolve is not contained in a generic container
    				mostPreciseResolutionClassName = staticClassName;
    			}
    		}
    		return mostPreciseResolutionClassName;
    	} catch (InvalidInputException e) {
    		//this should never happen
    		throw new UnexpectedInternalException(e);
    	}
    }
    
    /**
     * Solves the type information for a generic type variable.
     * 
     * @param hier a {@link ClassHierarchy}. It must not be {@code null}.
     * @param refToResolveContainer a {@link ReferenceSymbolic}, the 
     *        container of the reference to resolve. It must not be {@code null}.
     * @param typeParameterUnknown a {@link String}, the name of the type
     *        parameter for which we want to obtain the type information.
     * @return a {@link String}, the type of {@code typeParameterUnknown}
     *         obtained by solving the generic type equations derived by
     *         walking the container hierarchy spanned by {@code refToResolveContainer}.
     * @throws InvalidInputException if {@code hier == null || refToResolveContainer == null || typeParameterUnknown == null}.
     * @throws ClassFileNotFoundException if {@code hier} fails to load the erasure of the signature type
     *         of {@code refToResolveContainer} or of its containers, and throws {@link ClassFileNotFoundException}.
     * @throws ClassFileIllFormedException if {@code hier} fails to load the erasure of the signature type
     *         of {@code refToResolveContainer} or of its containers, and throws {@link ClassFileIllFormedException}.
     * @throws ClassFileNotAccessibleException if {@code hier} fails to load the erasure of the signature type
     *         of {@code refToResolveContainer} or of its containers, and throws {@link ClassFileNotAccessibleException}.
     * @throws IncompatibleClassFileException if {@code hier} fails to load the erasure of the signature type
     *         of {@code refToResolveContainer} or of its containers, and throws {@link IncompatibleClassFileException}.
     * @throws BadClassFileVersionException if {@code hier} fails to load the erasure of the signature type
     *         of {@code refToResolveContainer} or of its containers, and throws {@link BadClassFileVersionException}.
     * @throws RenameUnsupportedException if {@code hier} fails to load the erasure of the signature type
     *         of {@code refToResolveContainer} or of its containers, and throws {@link RenameUnsupportedException}.
     * @throws WrongClassNameException if {@code hier} fails to load the erasure of the signature type
     *         of {@code refToResolveContainer} or of its containers, and throws {@link WrongClassNameException}.
     * @throws DecisionException if this decision procedure fails.
     */
    private String solveTypeInformation(ClassHierarchy hier, ReferenceSymbolic refToResolveContainer, String typeParameterUnknown) 
    throws InvalidInputException, ClassFileNotFoundException, ClassFileIllFormedException, ClassFileNotAccessibleException, 
    IncompatibleClassFileException, BadClassFileVersionException, RenameUnsupportedException, WrongClassNameException, DecisionException {
    	if (hier == null || refToResolveContainer == null || typeParameterUnknown == null) {
    		throw new InvalidInputException("DecisionProcedureAlgorithm.solveTypeInformation: Invoked with a null parameter.");
    	}
    	
    	try {
    		//creates the solver
    		final SolverEquationGenericTypes solver = new SolverEquationGenericTypes();

    		//adds to the solver all the equations by walking the container 
    		//structure of the reference to resolve, starting from the outermost 
    		//container up to the innermost one. Every equation is formed 
    		//as follows: The symbolic reference's generic type signature 
    		//is equal to the generic type signature of the class of the 
    		//object it refers to. References to arrays, that are not 
    		//generic, are skipped.
    		final List<ReferenceSymbolic> refToResolveContainers = getContainerHierarchy(refToResolveContainer);
    		for (ReferenceSymbolic ref : refToResolveContainers) {
    			//gets the generic type signature of ref 
    			final String refGenericTypeSignature = ref.getGenericSignatureType();
    			if (refGenericTypeSignature == null || isArray(refGenericTypeSignature)) {
    				//in both cases no equation must be generated; Note that
    				//the first case happens when ref is a KlassPseudoRef (topmost)
    				continue;
    			}

    			//builds the left-hand side(s) of the equation:
    			//first, splits refGenericTypeSignature...
    			final String[] refGenericTypeSignatures = splitSignature(refGenericTypeSignature);

    			//...then, builds a term for each produced 
    			//type signature 
    			final TypeTerm[] lhs = new TypeTerm[refGenericTypeSignatures.length];
    			{
    				final String prefix = lhsPrefix(ref);
    				for (int i = 0; i < lhs.length; ++i) {
    					lhs[i] = referenceTypeSignatureToTypeTerm(prefix, refGenericTypeSignatures[i]);
    				}
    			}

    			//builds the right-hand side(s) of the equation:
    			//first, finds the type instantiations for all 
    			//the left-hand side generic type signatures...
    			final String[] refTypeInstantiations = findTypeInstantiations(hier, ref, getAssumptions()); 

    			//...then, builds the terms for them
    			final TypeTerm[] rhs = new TypeTerm[refTypeInstantiations.length];
    			{
    				final String prefix = ref.asOriginString();
    				for (int i = 0; i < rhs.length; ++i) {
    					rhs[i] = typeInstantiationToApply(prefix, refTypeInstantiations[i]);
    				}
    			}

    			//adds the equations
    			solver.addEquations(lhs, rhs);
    			
    			//TODO if refGenericTypeSignature is a method signature, and the method has generic parameters, it is necessary also to scan the arguments for additional constraints 
    		}

    		//solves
    		solver.solve();

    		//returns the solution, if there is one
    		if (solver.hasSolution()) {
    			final Var queryVariable = new Var(refToResolveContainer.asOriginString() + "?" + typeParameterUnknown);
    			final String solution = solver.getVariableValue(queryVariable);
    			return solution; 
    			//NB: solution may be null, typically when the query variable is not present in the equations, or when there are no or infinite solutions
    		} else {
    			return null;
    		}
    	} catch (InvalidInputException e) {
    		//this should never happen
    		throw new UnexpectedInternalException(e);
    	}
    }
    
    /**
     * Returns the complete container hierarchy of a symbolic reference. 
     * 
     * @param refStart a {@link ReferenceSymbolic}. It must not be {@code null}.
     * @return a {@link List}{@code <}{@link ReferenceSymbolic}{@code >}. 
     *         The last element is {@code refStart}, the (last - 1) element is
     *         {@code refStart}'s container, if it exists, and so on up to the
     *         outermost container, which is at position 0. The result is undefined
     *         if {@code refStart == null}. 
     */
    private static List<ReferenceSymbolic> getContainerHierarchy(ReferenceSymbolic refStart) {
    	final LinkedList<ReferenceSymbolic> retVal = new LinkedList<>();
    	ReferenceSymbolic ref = refStart;
    	retVal.push(ref);
    	while (ref instanceof ReferenceSymbolicMember) {
    		ref = ((ReferenceSymbolicMember) ref).getContainer();
        	retVal.push(ref);
    	}
    	return retVal;
    }
    
    /**
     * Splits a reference type signature or method signature into a set of reference 
     * type signatures that can be converted to {@link TypeTerm}s.
     *  
     * @param signature a {@link String}. It must not be {@code null}
     *        and it must be a reference type signature or
     *        a method signature for a method returning a reference. The definitions 
     *        of class type signature, type variable signature and method signature 
     *        are given in the grammar reported in the JVMS v8 section 4.7.9.1, 
     *        nonterminal symbols {@code ReferenceTypeSignature}, {@code ClassTypeSignature}, 
     *        {@code TypeVariableSignature}, {@code ArrayTypeSignature}, and {@code MethodSignature}.  
     * @return an array of {@link String}s. If {@code signature} is 
     *         a class type signature, the array will contain as many class type 
     *         signatures as the number of {@code ClassTypeSignatureSuffix}es plus one.
     *         For example, if {@code signature == "LA$B<...>.C$D$E<...>.F$G<...>;"}, 
     *         then an array with shape <code>{"LA$B<...>;", "LA$B$C$D$E<...>;", "LA$B$C$D$E$F$G<...>;"}</code> 
     *         will be returned. If {@code signature} is a type variable signature,
     *         an array with {@code signature} as its only element is returned. 
     *         If {@code signature} is an array type signature, an array with {@code signature} 
     *         as its only element is returned. 
     *         If {@code signature} is a method signature for a method returning a reference, 
     *         the signature for its return value (that is a reference type signature) will
     *         be processed as in the previous cases and the result returned. 
     *         The result is undefined if {@code referenceTypeSignature} is null, or is not
     *         a class type signature or a type variable signature.
     */
    private static String[] splitSignature(String signature) {
    	//this code assumes that signature is a method signature: 
    	//In this case extracts into returnValueSignature the signature
    	//of the return value of the method declaration; If signature
    	//is not a method signature, it will be returnValueSignature == signature.
    	final int rightParensIndex = signature.indexOf(')');
    	final int throwsIndex = signature.indexOf('^');
    	final String returnValueSignature = signature.substring(rightParensIndex + 1, (throwsIndex == -1 ? signature.length() : throwsIndex));
    	
    	//splits
		final String[] retVal = returnValueSignature.split("\\.");
		for (int i = 1; i < retVal.length; ++i) {
			retVal[i - 1] = (i == 1 ? "" : REFERENCE) + retVal[i - 1] + TYPEEND;
			final String s = retVal[i - 1].substring(1);
			final int end = s.indexOf('<') == -1 ? (s.length() - 1) : s.indexOf('<');
			retVal[i] = s.substring(0, end) + "$" + retVal[i];
		}
		if (retVal.length > 1) {
			retVal[retVal.length - 1] = REFERENCE + retVal[retVal.length - 1];
		}
		return retVal;
    }
    
    /**
     * Calculates the contextualization prefix for the
     * variables in the lhs of an equation.
     * 
     * @param referenceCurrentContainer a {@link ReferenceSymbolic}, 
     *        the current container. It must not be {@code null}.
     * @return a {@link String}. The result is undefined
     *         if {@code referenceCurrentContainer == null}.
     */
    private static String lhsPrefix(ReferenceSymbolic referenceCurrentContainer) {
		ReferenceSymbolic referenceNonArrayContainer = referenceCurrentContainer;
		while (referenceNonArrayContainer instanceof ReferenceSymbolicMemberArray) {
			referenceNonArrayContainer = ((ReferenceSymbolicMemberArray) referenceNonArrayContainer).getContainer();
		}
		final String retVal = (referenceNonArrayContainer instanceof ReferenceSymbolicMember ? ((ReferenceSymbolicMember) referenceNonArrayContainer).getContainer().asOriginString() : "");
		return retVal;
    }

	private static final TypeTerm[] EMPTY_TYPETERM_ARRAY = new TypeTerm[0];
	
	/**
	 * Converts a reference type signature to a {@link TypeTerm}.
	 * 
	 * @param prefix a {@link String} used to disambiguate (contextualize)
	 *        the type variables. It must not be {@code null}.  
	 * @param referenceTypeSignature a {@link String}. It must not be {@code null}
     *        and it must be a reference type signature.
	 * @return a {@link TypeTerm} corresponding to {@code referenceTypeSignature}, 
	 *         where the name of all the {@link Var}s occurring are qualified with 
	 *         the string {@code prefix + "?"}.
	 * @throws InvalidInputException if {@code prefix == null || referenceTypeSignature == null}, 
	 *         or if {@code referenceTypeSignature} is not a reference type signature.
	 */
	private static TypeTerm referenceTypeSignatureToTypeTerm(String prefix, String referenceTypeSignature) throws InvalidInputException {
		if (prefix == null || referenceTypeSignature == null) {
			throw new InvalidInputException("DecisionProcedureAlgorithm.referenceTypeSignatureToTypeTerm: Invoked with a null parameter.");
		} else if (referenceTypeSignature.charAt(0) == TYPEVAR) {
			return new Var(prefix + "?" + referenceTypeSignature.substring(1, referenceTypeSignature.length() - 1));
		} else if (referenceTypeSignature.charAt(0) == REFERENCE || referenceTypeSignature.charAt(0) == ARRAYOF) {
			final int begin = (referenceTypeSignature.charAt(0) == REFERENCE ? 1 : 0);
			final int langleIndex = referenceTypeSignature.indexOf('<');
			final int max = referenceTypeSignature.length() - begin;
			final String functor = referenceTypeSignature.substring(begin, (langleIndex == -1 ? max : langleIndex));
			if (langleIndex == -1) {
				return new Apply(functor);
			} else {
				final ArrayList<TypeTerm> args = new ArrayList<>();
				int i = functor.length() + 2;
				boolean unknown = false;
				while (i < referenceTypeSignature.length()) {
					final char c = referenceTypeSignature.charAt(i);
					if (c == '*') {
						args.add(referenceTypeSignatureToTypeTerm(prefix, referenceTypeSignature.substring(i, i + 1)));
						++i;
					} else if (c == '+' || c == '-') {
						unknown = true;
						++i;
					} else if (c == TYPEVAR) {
						final int start = i;
						do {
							++i;
						} while (referenceTypeSignature.charAt(i) != TYPEEND);
						args.add(unknown ? Some.instance() : referenceTypeSignatureToTypeTerm(prefix, referenceTypeSignature.substring(start, i + 1)));
						++i;
						unknown = false;
					} else if (c == REFERENCE) {
						final int start = i;
						int level = 1;
						do {
							++i;
							if (referenceTypeSignature.charAt(i) == '<') {
								++level;
							} else if (referenceTypeSignature.charAt(i) == '>') {
								--level;
							}
						} while (referenceTypeSignature.charAt(i) != TYPEEND || level != 1);
						args.add(unknown ? Some.instance() : referenceTypeSignatureToTypeTerm(prefix, referenceTypeSignature.substring(start, i + 1)));
						++i;
						unknown = false;
					} else if (c == ARRAYOF) {
						final int start = i;
						do {
							++i;
						} while (referenceTypeSignature.charAt(i) == ARRAYOF);
						if (referenceTypeSignature.charAt(i) == TYPEVAR || referenceTypeSignature.charAt(i) == REFERENCE) {
							int level = 1;
							do {
								++i;
								if (referenceTypeSignature.charAt(i) == '<') {
									++level;
								} else if (referenceTypeSignature.charAt(i) == '>') {
									--level;
								}
							} while (referenceTypeSignature.charAt(i) != TYPEEND || level != 1);
						}
						args.add(unknown ? Some.instance() : referenceTypeSignatureToTypeTerm(prefix, referenceTypeSignature.substring(start, i + 1)));
						++i;
						unknown = false;
					} else if (c == '>') {
						break;
					} else {
						throw new InvalidInputException("DecisionProcedureAlgorithm.referenceTypeSignatureToTypeTerm: Cannot parse as a generic type equational term the string: " + referenceTypeSignature + ".");
					}
				}
				return new Apply(functor, args.toArray(EMPTY_TYPETERM_ARRAY));
			}
		} else {
			throw new InvalidInputException("DecisionProcedureAlgorithm.referenceTypeSignatureToTypeTerm: Cannot parse as a generic type equational term the string: " + referenceTypeSignature + ".");
		}
	}
	
	/**
	 * Builds an array of <em>type instantiations</em> for the class pointed
	 * by a resolved reference.
	 * 
	 * @param hier a {@link ClassHierarchy}. It must not be {@code null}.
	 * @param reference a {@link ReferenceSymbolic}. It must not be {@code null}.
	 * @param pathCondition a {@link List}{@code <}{@link Clause}{@code >}, 
	 *        the current path condition. It must not be {@code null}, and
	 *        {@code reference} must be resolved by expansion in it.
	 * @return A {@link String}{@code []}. Every element in it is a <em>type
	 *         instantiation</em>, one for each element in 
	 *         {@link #splitSignature(String) splitReferenceTypeSignature}{@code (reference.}{@link ReferenceSymbolic#getGenericSignatureType() getGenericSignatureType}{@code ())}.
	 *         A type instantiation is a string formed by the concatenation 
	 *         of a class name and an optional list of type parameters, as 
	 *         defined by the nonterminal {@code TypeParameters} in the grammar 
	 *         defined in JVMS v8 section 4.7.9.1. 
	 * @throws InvalidInputException if {@code hier == null || reference == null || pathCondition == null}, 
	 *         or if {@code reference} has not an expands clause in {@code pathCondition}.
	 * @throws ClassFileNotFoundException if {@code hier} cannot load any class in the type erasures of
	 *         {@link #splitSignature(String) splitReferenceTypeSignature}{@code (reference.}{@link ReferenceSymbolic#getGenericSignatureType() getGenericSignatureType}{@code ())}.
	 *         and fails with a {@link ClassFileNotFoundException}.
	 * @throws ClassFileIllFormedException if {@code hier} cannot load any class in the type erasures of
	 *         {@link #splitSignature(String) splitReferenceTypeSignature}{@code (reference.}{@link ReferenceSymbolic#getGenericSignatureType() getGenericSignatureType}{@code ())}.
	 *         and fails with a {@link ClassFileIllFormedException}.
	 * @throws ClassFileNotAccessibleException if {@code hier} cannot load any class in the type erasures of
	 *         {@link #splitSignature(String) splitReferenceTypeSignature}{@code (reference.}{@link ReferenceSymbolic#getGenericSignatureType() getGenericSignatureType}{@code ())}.
	 *         and fails with a {@link ClassFileNotAccessibleException}.
	 * @throws IncompatibleClassFileException if {@code hier} cannot load any class in the type erasures of
	 *         {@link #splitSignature(String) splitReferenceTypeSignature}{@code (reference.}{@link ReferenceSymbolic#getGenericSignatureType() getGenericSignatureType}{@code ())}.
	 *         and fails with a {@link IncompatibleClassFileException}.
	 * @throws BadClassFileVersionException if {@code hier} cannot load any class in the type erasures of
	 *         {@link #splitSignature(String) splitReferenceTypeSignature}{@code (reference.}{@link ReferenceSymbolic#getGenericSignatureType() getGenericSignatureType}{@code ())}.
	 *         and fails with a {@link BadClassFileVersionException}.
	 * @throws RenameUnsupportedException if {@code hier} cannot load any class in the type erasures of
	 *         {@link #splitSignature(String) splitReferenceTypeSignature}{@code (reference.}{@link ReferenceSymbolic#getGenericSignatureType() getGenericSignatureType}{@code ())}.
	 *         and fails with a {@link RenameUnsupportedException}.
	 * @throws WrongClassNameException if {@code hier} cannot load any class in the type erasures of
	 *         {@link #splitSignature(String) splitReferenceTypeSignature}{@code (reference.}{@link ReferenceSymbolic#getGenericSignatureType() getGenericSignatureType}{@code ())}.
	 *         and fails with a {@link WrongClassNameException}.
	 */
	private static String[] findTypeInstantiations(ClassHierarchy hier, ReferenceSymbolic reference, List<Clause> pathCondition) 
	throws InvalidInputException, ClassFileNotFoundException, ClassFileIllFormedException, ClassFileNotAccessibleException, 
	IncompatibleClassFileException, BadClassFileVersionException, RenameUnsupportedException, WrongClassNameException {
		if (hier == null || reference == null || pathCondition == null) {
			throw new InvalidInputException("DecisionProcedureAlgorithm.findTypeInstantiations: Invoked with a null parameter.");
		}
		final String refGenericTypeSignature = reference.getGenericSignatureType();
		final String[] refGenericTypeSignatures = splitSignature(refGenericTypeSignature);
		final String[] retVal = new String[refGenericTypeSignatures.length];
		for (int i = 0; i < refGenericTypeSignatures.length; ++i) {
			final ClassFile curClass;
			if (isTypeParameter(refGenericTypeSignatures[i])) {
				//gets the class of the object pointed by ref, where the 
				//generic parameters are declared; Note that all the references 
				//in refToResolveContainers are resolved, so this operation 
				//always succeeds...
				ClassFile refClass = null;
				for (Clause c : pathCondition) {
					if (c instanceof ClauseAssumeExpands && reference.equals(((ClauseAssumeExpands) c).getReference())) {
						refClass = ((ClauseAssumeExpands) c).getObjekt().getType();
						break;
					}
				}
				if (refClass == null) {
					throw new InvalidInputException("DecisionProcedureAlgorithm.findTypeInstantiations: The reference parameter has no expands clause in path condition parameter.");
				}
				curClass = refClass;
			} else { //refGenericTypeSignatures[i] is a class type signature
				try {
					curClass = hier.loadCreateClass(CLASSLOADER_APP, className(eraseGenericParameters(refGenericTypeSignatures[i])), true);
				} catch (PleaseLoadClassException e) {
					//this should never happen
					throw new UnexpectedInternalException(e);
				}
			}
			final String curClassSignatureType = curClass.getGenericSignatureType();
			final String curClassTypeParameters;
			if (curClassSignatureType == null) {
				curClassTypeParameters = "";
			} else {
				curClassTypeParameters = splitClassGenericSignatureTypeParameters(curClassSignatureType);
			}
			retVal[i] = curClass.getClassName() + curClassTypeParameters;
		}
		return retVal;
	}
    
	private static final Var[] EMPTY_VAR_ARRAY = new Var[0];
    
	/**
	 * Converts a type instantiation to an {@link Apply} term.
	 * 
	 * @param prefix a {@link String} used to disambiguate (contextualize)
	 *        the type variables. It must not be {@code null}.  
	 * @param typeInstantiation a {@link String}. It must not be {@code null}
     *        and it must be a type instantiation (see the Javadoc for 
     *        {@link #findTypeInstantiations(ClassHierarchy, ReferenceSymbolic, List) findTypeInstantiations}
     *        for a definition of type instantiation).
	 * @return an {@link Apply}, whose functor is the class name in {@code typeInstantiation}, 
	 *         and whose arguments are a list of {@link Var}s, whose names 
	 *         are the names in the {@code TypeParameters} in {@code typeInstantiation}
	 *         qualified with the string {@code prefix + "?"}. The result is undefined if
	 *         {@code prefix == null || classNamePlusTypeVars == null} or 
	 *         {@code typeInstantiation} is not a type instantiation.
	 */
	private static Apply typeInstantiationToApply(String prefix, String typeInstantiation) {
		final int langleIndex = typeInstantiation.indexOf('<');
		final String functor = typeInstantiation.substring(0, (langleIndex == -1 ? typeInstantiation.length() : langleIndex));
		final String[] preVars = (langleIndex == -1 ? new String[0] : typeInstantiation.substring(langleIndex + 1).split(":"));
		final ArrayList<Var> vars = new ArrayList<>();
		for (int k = 0; k < preVars.length - 1; ++k) {
			vars.add(new Var(prefix + "?" + (k == 0 ? preVars[k] : preVars[k].substring(preVars[k].indexOf(';') + 1))));
		}

		return new Apply(functor, vars.toArray(EMPTY_VAR_ARRAY));
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

        //scans the path condition for compatible objects
        final List<Clause> pathCondition = getAssumptions();
        forAllInitialObjects(pathCondition, (object, heapPosition) -> {
            //if it is type and epoch compatible, adds the object
            //to the result
            try {
                if (isAliasCompatible(object, ref, refClass)) {
                    retVal.put(heapPosition, object);
                }
            } catch (InvalidInputException e) {
                //this should never happen (checked before)
                throw new UnexpectedInternalException(e);
            }
        });
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