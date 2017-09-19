package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.NEGATIVE_ARRAY_SIZE_EXCEPTION;

import java.util.function.Supplier;

import jbse.bc.ClassHierarchy;
import jbse.common.Type;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.dec.exc.DecisionException;
import jbse.dec.exc.InvalidInputException;
import jbse.mem.Array;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.tree.DecisionAlternative_XNEWARRAY;
import jbse.val.Calculator;
import jbse.val.Expression;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.ReferenceArrayImmaterial;
import jbse.val.ReferenceConcrete;
import jbse.val.Simplex;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * Abstract algorithm for the *newarray bytecodes
 * (newarray, anewarray, multianewarray). It decides over the
 * dimensions counts, a sheer numeric decision.
 * It is public because it is also used to implement the 
 * native {@code java.lang.reflect.Array.newArray} and 
 * {@code java.lang.reflect.Array.multiNewArray} methods.
 * 
 * @author Pietro Braione
 */
public abstract class Algo_XNEWARRAY<D extends BytecodeData> extends Algorithm<
D, 
DecisionAlternative_XNEWARRAY,
StrategyDecide<DecisionAlternative_XNEWARRAY>,
StrategyRefine<DecisionAlternative_XNEWARRAY>,
StrategyUpdate<DecisionAlternative_XNEWARRAY>> {

    //must be set by subclasses in preCook
    protected Primitive[] dimensionsCounts;
    protected String arrayType;

    private int layersToCreateNow; //produced by cook
    private Primitive countsNonNegative, countsNegative; //produced by cook

    /**
     * Subclasses implement this method to
     * check/set the dimensions count, to
     * set the array type, and possibly to 
     * resolve the member class.
     * 
     * @param state a {@link State}
     * @throws InterruptException if the execution of this {@link Algorithm}
     *         must be interrupted.
     */
    protected abstract void preCook(State state) throws InterruptException;

    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> {
            preCook(state);

            //calculates layersToCreateNow, i.e., the number of layers that 
            //can be created when this algorithm is executed; this number is
            //calculated by considering that the last layer that can be
            //created now is the first that has a symbolic length
            this.layersToCreateNow = 0;
            for (Primitive l : this.dimensionsCounts) {
                ++this.layersToCreateNow;
                if (!(l instanceof Simplex)) {
                    break;
                }
            }

            //builds countsNonNegative, countsNegative; this are two
            //boolean Primitives stating that all the dimension count 
            //values are nonnegative (respectively, negative)
            try {
                final Calculator calc = state.getCalculator();
                Primitive tmp = calc.valBoolean(true);
                for (Primitive l : this.dimensionsCounts) {
                    tmp = tmp.and(l.ge(calc.valInt(0)));
                }
                this.countsNonNegative = tmp;
                this.countsNegative = this.countsNonNegative.not();
            } catch (InvalidTypeException | InvalidOperandException e) {
                //TODO is it ok, or should we throw UnexpectedInternalException?
                throwVerifyError(state);
                exitFromAlgorithm();
            }
        };
    }

    @Override
    protected Class<DecisionAlternative_XNEWARRAY> classDecisionAlternative() {
        return DecisionAlternative_XNEWARRAY.class;
    }

    @Override
    protected StrategyDecide<DecisionAlternative_XNEWARRAY> decider() {
        return (state, result) -> {
            //invokes the decision procedure
            final Outcome o = this.ctx.decisionProcedure.decide_XNEWARRAY(state.getClassHierarchy(), this.countsNonNegative, result);
            return o;
        };
    }

    @Override
    protected StrategyRefine<DecisionAlternative_XNEWARRAY> refiner() {
        return (state, alt) -> {
            final Primitive condTrue = (alt.ok() ? this.countsNonNegative : this.countsNegative);
            state.assume(this.ctx.decisionProcedure.simplify(condTrue));
        };
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_XNEWARRAY> updater() {
        return (state, alt) -> {
            if (alt.ok()) {
                //calculates the number of dimensions as declared in arrayType
                //and checks that the length parameter does not exceed this number
                int dimDecl = Type.getDeclaredNumberOfDimensions(Algo_XNEWARRAY.this.arrayType);
                if (dimDecl < Algo_XNEWARRAY.this.dimensionsCounts.length) {
                    throwVerifyError(state);
                    exitFromAlgorithm();
                }

                //TODO check that arrayMemberSignature refers to an existing class (when should this be done???)
                //determines the signature of the array member
                //String arrayMemberSignature = arrayType.substring(dimDecl);
                //etc...

                //determines the initialization value
                Value initValue = null; //means default value
                if (this.layersToCreateNow < this.dimensionsCounts.length) {
                    //if it cannot create all layers now, initValue is a ReferenceArrayImmaterial,
                    //which will allow to lazily build layers upon access to them
                    final Primitive[] lConstr = new Primitive[this.dimensionsCounts.length - this.layersToCreateNow];
                    System.arraycopy(this.dimensionsCounts, this.layersToCreateNow, lConstr, 0, lConstr.length);
                    try {
                        initValue = new ReferenceArrayImmaterial(arrayType.substring(this.layersToCreateNow), lConstr);
                    } catch (InvalidTypeException e) {
                        //this should never happen
                        failExecution(e);
                    }
                }

                final ReferenceConcrete toPush;
                try {
                    //pushes the reference
                    toPush = createArrayMultilayer(state, initValue);
                    state.pushOperand(toPush);
                } catch (InvalidTypeException e) {
                    throwVerifyError(state);
                    exitFromAlgorithm();
                }
            } else {
                throwNew(state, NEGATIVE_ARRAY_SIZE_EXCEPTION);
                exitFromAlgorithm();
            }
        };

    }

    private ReferenceConcrete createArrayMultilayer(State state, Value initValue) 
    throws DecisionException, InvalidTypeException {
        //the reference to be pushed on the operand stack at the end of the
        //creation; note that it is initialized to null, but this is just 
        //to make the compiler happy. It will be initialized during the loop, 
        //at the end of which it must be nonnull 
        ReferenceConcrete retVal = null;

        //array of references to the Arrays created at the previous iteration
        Reference[] prev = null;

        //the number of arrays to be created in the current layer
        int toCreateInCurrentLayer = 1;

        //creates all the layers of monodimensional arrays that it can create now, 
        //initializes them, puts them in the heap, and sets toPush with a reference
        //to the topmost array
        final Calculator calc = state.getCalculator();
        for (int currentLayer = 0; currentLayer < this.layersToCreateNow; ++currentLayer) {
            //caches the length of the arrays in the current layer 
            final Primitive currentLayerLength = this.dimensionsCounts[currentLayer];

            //determines if such length is (concretely or provably) 0 to early stop the 
            //creation process
            final int currentLayerLengthInt; //to use only when currentLayerLength instanceof Simplex
            boolean zeroBreak = false; //only to keep the compiler happy
            if (currentLayerLength instanceof Simplex) {
                currentLayerLengthInt = ((Integer) ((Simplex) currentLayerLength).getActualValue());
                zeroBreak = (currentLayerLengthInt == 0); 
            } else {
                currentLayerLengthInt = -1; //not meaningful, set to an arbitrary value
                try {
                    final Expression currentLayerLengthZero = (Expression) currentLayerLength.eq(calc.valInt(0));
                    final Expression currentLayerLengthNonzero = (Expression) currentLayerLengthZero.not();
                    final ClassHierarchy hier = state.getClassHierarchy();
                    zeroBreak = this.ctx.decisionProcedure.isSat(hier, currentLayerLengthZero); 
                    zeroBreak = zeroBreak && !this.ctx.decisionProcedure.isSat(hier, currentLayerLengthNonzero);
                } catch (ClassCastException | InvalidOperandException | 
                InvalidTypeException | InvalidInputException e) {
                    //this should never happen
                    failExecution(e);
                } 
            }

            //creates the i-th layer of arrays
            final Reference[] next = new Reference[toCreateInCurrentLayer];
            for (int k = 0; k < toCreateInCurrentLayer; ++k) {
                //creates the k-th array in the layer
                final String subarrayType = this.arrayType.substring(currentLayer);
                final ReferenceConcrete ref = state.createArray(initValue, currentLayerLength, subarrayType);

                //stores the reference to the created array
                if (currentLayer == 0) { //topmost reference
                    retVal = ref;
                } else {				
                    //stores the reference in one of the arrays created 
                    //at (i-1)-th iteration; since only the objects in the
                    //last layer are not normalized, we may use the fast
                    //version of setting
                    final int prevArraySize = toCreateInCurrentLayer / prev.length; //size of arrays created at iteration i-1
                    final Simplex index = calc.valInt(k % prevArraySize);
                    try {
                        ((Array) state.getObject(prev[k /prevArraySize])).setFast(index, ref);
                    } catch (FastArrayAccessNotAllowedException | InvalidOperandException e) {
                        //this should never happen
                        failExecution(e);
                    }
                }

                //saves the created reference for initialization  
                //at (i+1)-th iteration
                next[k] = ref;
            }
            prev = next;
            if (currentLayerLength instanceof Simplex) {
                toCreateInCurrentLayer *= currentLayerLengthInt;
            }

            //exits if the length of the arrays in the current layer is zero
            if (zeroBreak) {
                break;
            }
        }

        return retVal;
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }
}
